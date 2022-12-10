package gregtech.common.pipelike.fluidpipe.longdistance;

import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.api.pipenet.longdist.MetaTileEntityLongDistanceEndpoint;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public boolean isValidBlock(IBlockState blockState) {
        return blockState.getBlock() == MetaBlocks.LD_FLUID_PIPE;
    }

    @Override
    public boolean isValidEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        return endpoint instanceof MetaTileEntityLDFluidEndpoint;
    }
}
