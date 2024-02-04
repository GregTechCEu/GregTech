package gregtech.common.metatileentities.multi.electric;

import static gregtech.api.util.RelativeDirection.*;

import gregtech.api.metatileentity.GCYMRecipeMapMultiblockController;

import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockLargeMultiblockCasing;

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
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityLargeWiremill extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeWiremill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.WIREMILL_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeWiremill(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "STX", "XXX")
                .aisle("XXX", "XCX", "XX#")
                .aisle("XXX", "XCX", "#X#")
                .aisle("XXX", "XXX", "#X#")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(25).or(autoAbilities()))
                .where('C', states(getCasingState2()))
                .where('T', tieredCasing().or(air()))
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING
                .getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_GEARBOX);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STRESS_PROOF_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return Textures.LARGE_WIREMILL_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }
}
