package gregtech.common.metatileentities.multi;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockNuclearCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntitySpentFuelPool extends RecipeMapMultiblockController {

    public MetaTileEntitySpentFuelPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.SPENT_FUEL_POOL_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySpentFuelPool(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("CCCCCCCCC", "CCCCSCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC")
                .aisle("CCCCCCCCC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC")
                .aisle("CCCCCCCCC", "CWRRRRRWC", "CWRRRRRWC", "CWRRRRRWC", "CWRRRRRWC", "CWRRRRRWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC").setRepeatable(1, 13)
                .aisle("CCCCCCCCC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC", "CWWWWWWWC")
                .aisle("CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC", "CCCCCCCCC")
                .where('S', selfPredicate())
                .where('C', states(getCasingState()))
                .where('W', states(getWaterState()).or(states(getFlowingWaterState())))
                .where('R', states(getRodState()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.AUTOCLAVE_OVERLAY;
    }

    private IBlockState getRodState() {
        return MetaBlocks.NUCLEAR_CASING.getState(BlockNuclearCasing.NuclearCasingType.SPENT_FUEL_CASING);
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    private IBlockState getWaterState() {
        return Blocks.WATER.getDefaultState();
    }

    private IBlockState getFlowingWaterState() {
        return Blocks.FLOWING_WATER.getDefaultState();
    }
}
