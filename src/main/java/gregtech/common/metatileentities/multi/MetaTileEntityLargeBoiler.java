package gregtech.common.metatileentities.multi;

import gregtech.api.capability.impl.BoilerRecipeLogic;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.utils.TooltipHelper;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase implements ProgressBarMultiblock {

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

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        // Steam Output line
                        ITextComponent steamOutput = TextComponentUtil.stringWithColor(
                                TextFormatting.AQUA,
                                TextFormattingUtil.formatNumbers(recipeLogic.getLastTickSteam()) + " L/t");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.large_boiler.steam_output",
                                steamOutput));

                        // Efficiency line
                        ITextComponent efficiency = TextComponentUtil.stringWithColor(
                                getNumberColor(recipeLogic.getHeatScaled()),
                                recipeLogic.getHeatScaled() + "%");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.large_boiler.efficiency",
                                efficiency));

                        // Throttle line
                        ITextComponent throttle = TextComponentUtil.stringWithColor(
                                getNumberColor(getThrottle()),
                                getThrottle() + "%");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.large_boiler.throttle",
                                throttle));
                    }
                })
                .addWorkingStatusLine();
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
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed()) {
            if (getWaterFilled() == 0) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.large_boiler.no_water"));
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "gregtech.multiblock.large_boiler.explosion_tooltip"));
            }
        }
    }

    @Override
    protected MultiblockUIFactory<MultiblockWithDisplayBase> createUIFactory() {
        IntSyncValue syncValue = new IntSyncValue(this::getThrottlePercentage, this::setThrottlePercentage);
        return new MultiblockUIFactory<>(this) {

            @Override
            public @Nullable Widget<?> createFlexButton(@NotNull ModularPanel mainPanel,
                                                        @NotNull PanelSyncManager panelSyncManager) {
                PanelSyncHandler panel = panelSyncManager.panel("throttle_panel", mainPanel,
                        (syncManager, syncHandler) -> GTGuis.createPopupPanel("boiler_throttle", 116, 53)
                                .child(new Row()
                                        .pos(4, 4)
                                        .height(16).coverChildrenWidth()
                                        .child(new ItemDrawable(getStackForm())
                                                .asWidget()
                                                .size(16)
                                                .marginRight(4))
                                        .child(IKey.lang("gregtech.multiblock.large_boiler.throttle.title")
                                                .asWidget()
                                                .heightRel(1.0f)))
                                .child(new Row()
                                        // TODO add inc/dec buttons
                                        .child(new TextFieldWidget()
                                                .size(40, 20)
                                                .left(38)
                                                .top(20)
                                                // TODO proper color
                                                .setTextColor(Color.WHITE.darker(1))
                                                .setNumbers(0, 100)
                                                // TODO show % sign
                                                .value(syncValue)
                                                .background(GTGuiTextures.DISPLAY))));

                return new ButtonWidget<>()
                        .width(18)
                        .overlay(GTGuiTextures.BUTTON_THROTTLE_MINUS)
                        // TODO make this work
                        .background(GTGuiTextures.BUTTON)
                        .onMousePressed(i -> {
                            if (panel.isPanelOpen()) {
                                panel.closePanel();
                            } else {
                                panel.openPanel();
                            }
                            Interactable.playButtonClickSound();
                            return true;
                        });
            }
        };
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
    public @NotNull ProgressWidget createProgressBar(PanelSyncManager panelSyncManager, int index) {
        IntSyncValue waterFilledValue = new IntSyncValue(this::getWaterFilled, null);
        IntSyncValue waterCapacityValue = new IntSyncValue(this::getWaterCapacity, null);
        panelSyncManager.syncValue("water_filled", waterFilledValue);
        panelSyncManager.syncValue("water_capacity", waterCapacityValue);

        return new ProgressWidget()
                .progress(() -> waterCapacityValue.getIntValue() == 0 ? 0 :
                        waterFilledValue.getIntValue() * 1.0 / waterCapacityValue.getIntValue())
                .texture(GTGuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION, MultiblockUIFactory.Bars.FULL_WIDTH)
                .tooltipBuilder(t -> {
                    t.setAutoUpdate(true);
                    if (isStructureFormed()) {
                        if (waterFilledValue.getIntValue() == 0) {
                            t.addLine(IKey.lang("gregtech.multiblock.large_boiler.no_water"));
                        } else {
                            t.addLine(IKey.lang("gregtech.multiblock.large_boiler.water_bar_hover",
                                    waterFilledValue.getIntValue(), waterCapacityValue.getIntValue()));
                        }
                    } else {
                        t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                    }
                });
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
}
