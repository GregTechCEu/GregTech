package gregtech.common.metatileentities.transport;

import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.common.blocks.transport.LongDistanceItemPipeBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LongDistItemPipeWalker extends LongDistancePipeWalker {

    public LongDistItemPipeWalker(World world, BlockPos start, int walkedBlocks) {
        super(world, start, walkedBlocks);
    }

    @Override
    protected LongDistancePipeWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        return new LongDistItemPipeWalker(world, nextPos, walkedBlocks);
    }

    @Override
    public boolean isValidEndpoint(MetaTileEntityPipelineEndpoint endpoint) {
        return endpoint instanceof MetaTileEntityPipelineItem;
    }

    @Override
    protected boolean isValidPipe(IBlockState blockState, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return blockState.getBlock() instanceof LongDistanceItemPipeBlock;
    }
}
