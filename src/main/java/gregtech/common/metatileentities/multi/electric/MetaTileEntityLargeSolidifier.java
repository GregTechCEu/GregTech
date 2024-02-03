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
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityLargeSolidifier extends GCYMRecipeMapMultiblockController {

    public MetaTileEntityLargeSolidifier(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FLUID_SOLIDFICATION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityLargeSolidifier(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("#XXX#", "XXXXX", "XXXXX", "XXXXX", "#XXX#")
                .aisle("#XSX#", "XCTCX", "XAAAX", "XCACX", "#XXX#")
                .aisle("#XXX#", "XCACX", "XAAAX", "XCACX", "#XXX#")
                .aisle("#XXX#", "XXXXX", "XXXXX", "XXXXX", "#XXX#")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(45).or(autoAbilities()))
                .where('C', states(getCasingState2()))
                .where('T', tieredCasing().or(air()))
                .where('A', air())
                .where('#', any())
                .build();
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.WATERTIGHT_CASING);
    }

    private static IBlockState getCasingState2() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.WATERTIGHT_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return Textures.LARGE_SOLIDIFIER_OVERLAY;
    }
}
