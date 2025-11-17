package gregtech.common.metatileentities.multi;

import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.BoilerRecipeLogic;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.ui.KeyManager;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIBuilder;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.metatileentity.multiblock.ui.UISyncer;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase implements ProgressBarMultiblock,
                                       IControllable {

    public final BoilerType boilerType;
    protected BoilerRecipeLogic recipeLogic;
    private FluidTankList fluidImportInventory;
    private ItemHandlerList itemImportInventory;
    private FluidTankList steamOutputTank;

    private int throttlePercentage = 100;

    public MetaTileEntityLargeBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.recipeLogic = new BoilerRecipeLogic(this);
        resetTileAbilities();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.throttlePercentage = 100;
        this.recipeLogic.invalidate();
    }

    private void initializeAbilities() {
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.steamOutputTank = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    private void resetTileAbilities() {
        this.fluidImportInventory = new FluidTankList(true);
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.steamOutputTank = new FluidTankList(true);
    }

    private TextFormatting getNumberColor(int number) {
        if (number == 0) {
            return TextFormatting.DARK_RED;
        } else if (number <= 40) {
            return TextFormatting.RED;
        } else if (number < 100) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.GREEN;
        }
    }

    @Override
    protected void configureDisplayText(MultiblockUIBuilder builder) {
        builder.setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addCustom(this::addCustomData)
                .addWorkingStatusLine();
    }

    @Override
    protected void configureWarningText(MultiblockUIBuilder builder) {
        super.configureWarningText(builder);
        builder.addCustom((manager, syncer) -> {
            if (isStructureFormed() && syncer.syncBoolean(getWaterFilled() == 0)) {
                manager.add(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.large_boiler.no_water"));
                manager.add(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.large_boiler.explosion_tooltip"));
            }
        });
    }

    @Override
    public GTGuiTheme getUITheme() {
        return switch (this.boilerType) {
            case BRONZE -> GTGuiTheme.BRONZE;
            case STEEL -> GTGuiTheme.STEEL;
            default -> super.getUITheme();
        };
    }

    @Override
    protected MultiblockUIFactory createUIFactory() {
        return super.createUIFactory()
                .createFlexButton((guiData, syncManager) -> {
                    var throttle = syncManager.panel("throttle_panel", this::makeThrottlePanel, true);

                    return new ButtonWidget<>()
                            .size(18)
                            .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                            .addTooltipLine(IKey.lang("gregtech.multiblock.large_boiler.throttle_button.tooltip"))
                            .onMousePressed(i -> {
                                if (throttle.isPanelOpen()) {
                                    throttle.closePanel();
                                } else {
                                    throttle.openPanel();
                                }
                                return true;
                            });
                });
    }

    private void addCustomData(KeyManager keyManager, UISyncer syncer) {
        if (isStructureFormed()) {
            int steam = syncer.syncInt(recipeLogic.getLastTickSteam());
            int heatScaled = syncer.syncInt(recipeLogic.getHeatScaled());
            int throttleAmt = syncer.syncInt(getThrottle());

            // Steam Output line
            IKey steamOutput = KeyUtil.number(TextFormatting.AQUA,
                    steam, " L/t");

            keyManager.add(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.large_boiler.steam_output", steamOutput));

            // Efficiency line
            IKey efficiency = KeyUtil.number(
                    getNumberColor(heatScaled), heatScaled, "%");
            keyManager.add(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.large_boiler.efficiency", efficiency));

            // Throttle line
            IKey throttle = KeyUtil.number(
                    getNumberColor(throttleAmt),
                    throttleAmt, "%");
            keyManager.add(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.large_boiler.throttle", throttle));
        }
    }

    private ModularPanel makeThrottlePanel(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        StringSyncValue throttleValue = new StringSyncValue(() -> throttlePercentage + "%", str -> {
            try {
                if (str.charAt(str.length() - 1) == '%') {
                    str = str.substring(0, str.length() - 1);
                }

                this.throttlePercentage = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {

            }
        });
        DoubleSyncValue sliderValue = new DoubleSyncValue(
                () -> (double) getThrottlePercentage() / 100,
                d -> setThrottlePercentage((int) (d * 100)));

        return GTGuis.createPopupPanel("boiler_throttle", 116, 53)
                .child(Flow.row()
                        .pos(4, 4)
                        .height(16)
                        .coverChildrenWidth()
                        .child(new ItemDrawable(getStackForm())
                                .asWidget()
                                .size(16)
                                .marginRight(4))
                        .child(IKey.lang("gregtech.multiblock.large_boiler.throttle.title")
                                .asWidget()
                                .heightRel(1.0f)))
                .child(Flow.row()
                        .top(20)
                        .margin(4, 0)
                        .coverChildrenHeight()
                        .child(new SliderWidget()
                                .background(new Rectangle().setColor(Color.BLACK.brighter(2)).asIcon()
                                        .height(8))
                                .bounds(0, 1)
                                .setAxis(GuiAxis.X)
                                .value(sliderValue)
                                .widthRel(0.7f)
                                .height(20))
                        // todo switch this text field with GTTextFieldWidget in PR #2700
                        .child(new TextFieldWidget()
                                .widthRel(0.3f)
                                .height(20)
                                // TODO proper color
                                .setTextColor(Color.WHITE.darker(1))
                                .setValidator(str -> {
                                    if (str.charAt(str.length() - 1) == '%') {
                                        str = str.substring(0, str.length() - 1);
                                    }

                                    try {
                                        long l = Long.parseLong(str);
                                        if (l < 0) l = 0;
                                        else if (l > 100) l = 100;
                                        return String.valueOf(l);
                                    } catch (NumberFormatException ignored) {
                                        return throttleValue.getValue();
                                    }
                                })
                                .value(throttleValue)
                                .background(GTGuiTextures.DISPLAY)));
    }

    private void setThrottlePercentage(int amount) {
        this.throttlePercentage = amount;
    }

    private int getThrottlePercentage() {
        return this.throttlePercentage;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeLogic.isActive() && recipeLogic.isWorkingEnabled();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "CCC")
                .aisle("XXX", "CPC", "CPC", "CCC")
                .aisle("XXX", "CSC", "CCC", "CCC")
                .where('S', selfPredicate())
                .where('P', states(boilerType.pipeState))
                .where('X', states(boilerType.fireboxState).setMinGlobalLimited(4)
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(autoAbilities())) // muffler, maintenance
                .where('C', states(boilerType.casingState).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[] { I18n.format("gregtech.multiblock.large_boiler.description") };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.rate_tooltip",
                (int) (boilerType.steamPerTick() * 20 * boilerType.runtimeBoost(20) / 20.0)));
        tooltip.add(
                I18n.format("gregtech.multiblock.large_boiler.heat_time_tooltip", boilerType.getTicksToBoiling() / 20));
        tooltip.add(I18n.format("gregtech.universal.tooltip.base_production_fluid", boilerType.steamPerTick()));
        tooltip.add(TooltipHelper.BLINKING_RED + I18n.format("gregtech.multiblock.large_boiler.explosion_tooltip"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                recipeLogic.isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return boilerType.frontOverlay;
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart != null && isFireboxPart(sourcePart)) {
            return isActive() ? boilerType.fireboxActiveRenderer : boilerType.fireboxIdleRenderer;
        }
        return boilerType.casingRenderer;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.BOILER;
    }

    @Override
    protected void updateFormedValid() {
        this.recipeLogic.update();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("ThrottlePercentage", throttlePercentage);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        throttlePercentage = data.getInteger("ThrottlePercentage");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(throttlePercentage);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        throttlePercentage = buf.readVarInt();
    }

    public int getThrottle() {
        return throttlePercentage;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return itemImportInventory;
    }

    @Override
    public FluidTankList getImportFluids() {
        return fluidImportInventory;
    }

    @Override
    public FluidTankList getExportFluids() {
        return steamOutputTank;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof BoilerRecipeLogic);
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public int getProgressBarCount() {
        return 1;
    }

    @Override
    public void registerBars(List<UnaryOperator<ProgressWidget>> bars, PanelSyncManager syncManager) {
        IntSyncValue waterFilledValue = new IntSyncValue(this::getWaterFilled);
        IntSyncValue waterCapacityValue = new IntSyncValue(this::getWaterCapacity);
        syncManager.syncValue("water_filled", waterFilledValue);
        syncManager.syncValue("water_capacity", waterCapacityValue);

        bars.add(progressWidget -> progressWidget
                .progress(() -> waterCapacityValue.getIntValue() == 0 ? 0 :
                        waterFilledValue.getIntValue() * 1.0 / waterCapacityValue.getIntValue())
                .texture(GTGuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION, -1)
                .tooltipBuilder(tooltip -> {
                    if (isStructureFormed()) {
                        if (waterFilledValue.getIntValue() == 0) {
                            tooltip.addLine(IKey.lang("gregtech.multiblock.large_boiler.no_water"));
                        } else {
                            tooltip.addLine(IKey.lang("gregtech.multiblock.large_boiler.water_bar_hover",
                                    waterFilledValue.getIntValue(), waterCapacityValue.getIntValue()));
                        }
                    } else {
                        tooltip.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                    }
                }));
    }

    /**
     * @return the total amount of water filling the inputs
     */
    private int getWaterFilled() {
        if (!isStructureFormed()) return 0;
        List<IFluidTank> tanks = getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        int filled = 0;
        for (IFluidTank tank : tanks) {
            if (tank == null || tank.getFluid() == null) continue;
            if (CommonFluidFilters.BOILER_FLUID.test(tank.getFluid())) {
                filled += tank.getFluidAmount();
            }
        }
        return filled;
    }

    /**
     * @return the total capacity for water-containing inputs
     */
    private int getWaterCapacity() {
        if (!isStructureFormed()) return 0;
        List<IFluidTank> tanks = getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        int capacity = 0;
        for (IFluidTank tank : tanks) {
            if (tank == null || tank.getFluid() == null) continue;
            if (CommonFluidFilters.BOILER_FLUID.test(tank.getFluid())) {
                capacity += tank.getCapacity();
            }
        }
        return capacity;
    }

    @Override
    public boolean isWorkingEnabled() {
        return recipeLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        recipeLogic.setWorkingEnabled(isWorkingAllowed);
    }
}
