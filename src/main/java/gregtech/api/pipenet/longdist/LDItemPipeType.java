package gregtech.api.pipenet.longdist;

import net.minecraft.block.state.IBlockState;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public boolean isValidBlock(IBlockState blockState) {
        return blockState.getBlock() instanceof BlockLongDistancePipe;
    }

    @Override
    public boolean isValidEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        return endpoint instanceof MetaTileEntityLDItemEndpoint;
    }
}
