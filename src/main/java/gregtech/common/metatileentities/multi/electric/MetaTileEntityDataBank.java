package gregtech.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class MetaTileEntityDataBank extends MultiblockWithDisplayBase {

    public MetaTileEntityDataBank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataBank(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XDDDX", "XDDDX", "XDDDX")
                .aisle("XDDDX", "XAAAX", "XDDDX")
                .aisle("XCCCX", "XCSCX", "XCCCX")
                .where('S', selfPredicate())
                .where('X', states(getInnerState()))
                .where('D', states(getOuterState())
                        .or(abilities(MultiblockAbility.DATA_ACCESS_HATCH)))
                .where('A', states(getInnerState()))
                .where('C', states(getInnerState())
                        .or(autoAbilities()))
                .build();
    }

    @Nonnull
    private static IBlockState getInnerState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Nonnull
    private static IBlockState getOuterState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;//TODO
    }
}
