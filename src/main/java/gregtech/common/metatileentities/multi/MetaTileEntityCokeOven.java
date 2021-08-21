package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityCokeOven extends RecipeMapPrimitiveMultiblockController {

    public MetaTileEntityCokeOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COKE_OVEN_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCokeOven(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XZX", "XXX")
                .aisle("XZX", "Z#Z", "XZX")
                .aisle("XXX", "XYX", "XXX")
                .where('Z', statePredicate(getCasingState()).or(tilePredicate((state, tile) -> tile instanceof MetaTileEntityCokeOvenHatch)))
                .where('X', statePredicate(getCasingState()))
                .where('#', isAirPredicate())
                .where('Y', selfPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null && recipeMapWorkable.isActive() ? 15 : 0;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.COKE_OVEN_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive());
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .widget(new SlotWidget(importItems, 0, 33, 30, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FURNACE_OVERLAY))
                .progressBar(recipeMapWorkable::getProgressPercent, 58, 30, 20, 15, GuiTextures.BRONZE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL)
                .widget(new SlotWidget(exportItems, 0, 85, 30, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FURNACE_OVERLAY))
                .widget(new TankWidget(exportFluids.getTankAt(0), 133, 13, 20, 58)
                        .setBackgroundTexture(GuiTextures.FLUID_TANK_BACKGROUND)
                        .setOverlayTexture(GuiTextures.FLUID_TANK_OVERLAY)
                        .setContainerClicking(true, false))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0);
    }
}
