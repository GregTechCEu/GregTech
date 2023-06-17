package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityDataBank extends MultiblockWithDisplayBase {

    public MetaTileEntityDataBank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataBank(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {}

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XDDDX", "XDDDX", "XDDDX")
                .aisle("XDDDX", "XAAAX", "XDDDX")
                .aisle("XCCCX", "XCSCX", "XCCCX")
                .where('S', selfPredicate())
                .where('X', states(getOuterState()))
                .where('D', states(getInnerState()).setMinGlobalLimited(3)
                        .or(abilities(MultiblockAbility.DATA_ACCESS_HATCH))
                        .or(abilities(MultiblockAbility.OPTICAL_DATA_ACCESS_HATCH)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                )
                .where('A', states(getInnerState()))
                .where('C', states(getFrontState())
                        .setMinLayerLimited(4)
                        .or(autoAbilities())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                .build();
    }

    @Nonnull
    private static IBlockState getOuterState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT);
    }

    @Nonnull
    private static IBlockState getInnerState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    private static IBlockState getFrontState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IDataAccessHatch) {
            return Textures.COMPUTER_CASING;
        }
        return Textures.HIGH_POWER_CASING;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.DATA_BANK_OVERLAY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.data_bank.tooltip.3"));
    }
}
