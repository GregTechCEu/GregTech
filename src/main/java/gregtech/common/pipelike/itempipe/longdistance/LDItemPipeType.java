package gregtech.common.pipelike.itempipe.longdistance;

import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.common.ConfigHolder;
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
    public boolean isValidEndpoint(ILDEndpoint endpoint) {
        return endpoint instanceof MetaTileEntityLDItemEndpoint;
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.machines.ldItemPipeMinDistance;
    }
}
