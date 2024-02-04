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

public class MetaTileEntityLargeReplicator extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeReplicator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.REPLICATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeReplicator(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXX", "XVVVX", "XGGGX", "XGGGX", "XVVVX", "XXXXX")
                .aisle("XXXXX", "VCCCV", "GAAAG", "GAAAG", "VCCCV", "XXXXX")
                .aisle("XXXXX", "VCCCV", "GACAG", "GACAG", "VCCCV", "XXXXX")
                .aisle("XXXXX", "VCCCV", "GAAAG", "GAAAG", "VCCCV", "XXXXX")
                .aisle("XXSXX", "XVVVX", "XGGGX", "XGGGX", "XVVVX", "XXXXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(50).or(autoAbilities()))
                .where('C', states(getCasingState2()))
                .where('G', states(getCasingState3()))
                .where('V', states(getCasingState4()))
                .where('A', air())
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.ATOMIC_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_COIL);
    }

    private static IBlockState getCasingState3() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    private static IBlockState getCasingState4() {
        return MetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT);
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
