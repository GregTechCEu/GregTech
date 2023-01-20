package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import scala.Int;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityFlotationCell extends RecipeMapMultiblockController {
    private static final int MAX_PARALLEL = 128;

    public MetaTileEntityFlotationCell(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FLOTATION_RECIPES);
        this.recipeMapWorkable = new FlotationCellRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFlotationCell(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FXXXF", "FXAXF", "FAAAF", "FXAXF", "FXXXF")
                .aisle("XXXXX", "X   X", "A   A", "X   X", "X P X")
                .aisle("XXXXX", "A P A", "A P A", "A P A", "APPPA")
                .aisle("XXXXX", "X   X", "A   A", "X   X", "X P X")
                .aisle("FXXXF", "FXAXF", "FASAF", "FXAXF", "FXXXF")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(14).or(autoAbilities()))
                .where('F', frames(Materials.Titanium))
                .where('A', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE)))
                .where(' ', air())
                .build();
    }
    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.flotation_cell.tooltip.1"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", Integer.toString(MAX_PARALLEL)));
    }

    private class FlotationCellRecipeLogic extends MultiblockRecipeLogic {
        public FlotationCellRecipeLogic(MetaTileEntityFlotationCell tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return MAX_PARALLEL;
        }
    }
}
