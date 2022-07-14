package gregtech.common.blocks.transport;

import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.common.metatileentities.transport.LongDistItemPipeWalker;
import gregtech.common.metatileentities.transport.LongDistancePipeWalker;
import gregtech.common.metatileentities.transport.MetaTileEntityPipelineItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class LongDistanceItemPipeBlock extends LongDistancePipelineBlock {

    public LongDistanceItemPipeBlock() {
        setTranslationKey("long_distance_item_pipeline");
    }

    @Override
    protected LongDistancePipeWalker getPipeWalker(World world, BlockPos pos) {
        return new LongDistItemPipeWalker(world, pos, 0);
    }

    @Override
    protected boolean isPipeBlockValid(IBlockState block) {
        return block.getBlock() instanceof LongDistanceItemPipeBlock;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            Pair<MetaTileEntityPipelineEndpoint, Integer> endpoint = MetaTileEntityPipelineEndpoint.scanPipes(worldIn, pos, null, state1 -> state1.getBlock() instanceof LongDistanceItemPipeBlock, endpoint1 -> endpoint1 instanceof MetaTileEntityPipelineItem);
            if (endpoint != null) {
                endpoint.getKey().onPipeBlockChanged();
            }
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            Pair<MetaTileEntityPipelineEndpoint, Integer> endpoint = MetaTileEntityPipelineEndpoint.scanPipes(worldIn, pos, null, state1 -> state1.getBlock() instanceof LongDistanceItemPipeBlock, endpoint1 -> endpoint1 instanceof MetaTileEntityPipelineItem);
            if (endpoint != null) {
                endpoint.getKey().onPipeBlockChanged();
            }
        }
    }
}
