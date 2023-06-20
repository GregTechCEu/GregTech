package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

public class MetaTileEntityCoolantHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IFluidTank>, IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private boolean valid;
    private final FluidTank fluidTank;

    public MetaTileEntityCoolantHatch(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 4, isExportHatch);
        this.fluidTank = new NotifiableFluidTank(16000, this, isExportHatch);
        this.frontFacing = isExportHatch ? EnumFacing.DOWN : EnumFacing.UP;
    }
    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoolantHatch(metaTileEntityId, isExportHatch);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI(fluidTank, getMetaFullName(), entityPlayer).build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, String title, EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.label(6, 6, title)
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 16, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 90, 53, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.FLUID_HATCH_OUTPUT_OVERLAY : Textures.FLUID_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(isExportHatch ? EnumFacing.DOWN : EnumFacing.UP);
    }

    @Override
    public boolean checkValidity(int depth) {
        //Export ports are always considered valid
        if(isExportHatch) return true;
        BlockPos pos = this.getPos();
        for(int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.offset(EnumFacing.DOWN, i)) != MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL)) {
                return false;
            }
        }
        if (getWorld().getTileEntity(pos.offset(EnumFacing.DOWN, depth)) instanceof IGregTechTileEntity gtTe) {
            return gtTe.getMetaTileEntity().metaTileEntityId.equals(MetaTileEntities.COOLANT_OUTPUT.metaTileEntityId);
        }
        return false;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_COOLANT : MultiblockAbility.IMPORT_COOLANT;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.add(fluidTank);
    }
}
