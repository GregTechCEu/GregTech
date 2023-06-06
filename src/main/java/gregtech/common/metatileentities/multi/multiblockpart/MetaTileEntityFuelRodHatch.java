package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class MetaTileEntityFuelRodHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable>, IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private boolean valid;

    public MetaTileEntityFuelRodHatch(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 4, isExportHatch);
        this.frontFacing = EnumFacing.UP;
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
        return new MetaTileEntityFuelRodHatch(metaTileEntityId, isExportHatch);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,143)
                .label(10, 5, getMetaFullName());

        builder.widget(new SlotWidget(isExportHatch ? exportItems : importItems, 0, 79, 18, true, !isExportHatch)
                .setBackgroundTexture(GuiTextures.SLOT));

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 60);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.ITEM_HATCH_OUTPUT_OVERLAY : Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {

    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(EnumFacing.UP);
    }

    @Override
    public boolean checkValidity(int depth) {
        //Export ports are always considered valid
        if(isExportHatch) return true;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        for(int i = 0; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(EnumFacing.DOWN, i)) != MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL)) {
                return false;
            }
        }
        if (getWorld().getTileEntity(pos.move(EnumFacing.DOWN, depth)) instanceof IGregTechTileEntity gtTe) {
            return gtTe.getMetaTileEntity() == MetaTileEntities.FUEL_ROD_OUTPUT;
        }
        return false;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
