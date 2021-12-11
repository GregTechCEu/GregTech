package gregtech.api.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import gregtech.api.util.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SimpleGeneratorMetaTileEntity2 extends WorkableTieredMetaTileEntity implements IActiveOutputSide {

    private final boolean hasFrontFacing;

    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;

    private static final int FONT_HEIGHT = 9; // Minecraft's FontRenderer FONT_HEIGHT value

    public SimpleGeneratorMetaTileEntity2(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer, int tier, boolean hasFrontFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    public SimpleGeneratorMetaTileEntity2(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer, int tier, boolean hasFrontFacing,
                                          Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
        this.hasFrontFacing = hasFrontFacing;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new SimpleGeneratorMetaTileEntity2(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(), hasFrontFacing, getTankScalingFunction());
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        final FuelRecipeLogic2 result = new FuelRecipeLogic2(this, recipeMap, () -> energyContainer);
        result.enableOverclockVoltage();
        result.setParallelLimit((int) Math.max(1, Math.pow(4, getTier() - 1)));
        return result;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new ItemStackHandler(0), exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(side -> side == getFrontFacing());
    }

    @Override
    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (fluidInventory.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
            }
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (itemInventory.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemInventory);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> workableRecipeMap = workable.getRecipeMap();
        int yOffset = 0;
        if (workableRecipeMap.getMaxInputs() >= 6 || workableRecipeMap.getMaxFluidInputs() >= 6 || workableRecipeMap.getMaxOutputs() >= 6 || workableRecipeMap.getMaxFluidOutputs() >= 6) {
            yOffset = FONT_HEIGHT;
        }

        ModularUI.Builder builder = workableRecipeMap.createUITemplate(workable::getProgressPercent, importItems, exportItems, importFluids, exportFluids, yOffset)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new ImageWidget(79, 42 + yOffset, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)
                        .setPredicate(workable::isHasNotEnoughEnergy))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);

        int leftButtonStartX = 7;

        builder.widget(new CycleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                workable.getAvailableOverclockingTiers(), workable::getOverclockTier, workable::setOverclockTier)
                .setTooltipHoverString("gregtech.gui.overclock.description")
                .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));

        return builder;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGuiTemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        String key = this.metaTileEntityId.getPath().split("\\.")[0];
        tooltip.add(1, I18n.format(String.format("gregtech.machine.%s.tooltip", key)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), GTValues.VN[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.getTankScalingFunction().apply(getTier())));
    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }
}
