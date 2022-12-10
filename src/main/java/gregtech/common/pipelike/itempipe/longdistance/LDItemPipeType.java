package gregtech.common.pipelike.itempipe.longdistance;

import gregtech.api.pipenet.longdist.BlockLongDistancePipe;
import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.api.pipenet.longdist.MetaTileEntityLongDistanceEndpoint;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public boolean isValidBlock(IBlockState blockState) {
        return blockState.getBlock() == MetaBlocks.LD_ITEM_PIPE;
    }

    @Override
    public boolean isValidEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        return endpoint instanceof MetaTileEntityLDItemEndpoint;
    }
}
