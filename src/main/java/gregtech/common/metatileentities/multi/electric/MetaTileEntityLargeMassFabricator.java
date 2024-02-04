package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.GCYMRecipeMapMultiblockController;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockLargeMultiblockCasing;

import gregtech.common.blocks.BlockUniqueCasing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityLargeMassFabricator extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeMassFabricator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.MASS_FABRICATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeMassFabricator(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XGGGX", "XGVGX", "XGGGX", "XXXXX")
                .aisle("XXXXX", "GAAAG", "GAKAG", "GAAAG", "XXXXX")
                .aisle("XXVXX", "GAKAG", "VKKKV", "GAKAG", "XXVXX")
                .aisle("XXXXX", "GAAAG", "GAKAG", "GAAAG", "XXXXX")
                .aisle("XXXXX", "XGGGX", "XGSGX", "XGGGX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(45).or(autoAbilities()))
                .where('G', states(getCasingState2()))
                .where('V', states(getCasingState3()))
                .where('K', states(getCasingState4()))
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.ATOMIC_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    private static IBlockState getCasingState3() {
        return MetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT);
    }

    private static IBlockState getCasingState4() {
        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_COIL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.ATOMIC_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return Textures.LARGE_MASS_FABRICATOR_OVERLAY;
    }
}
