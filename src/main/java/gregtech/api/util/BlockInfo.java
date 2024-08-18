package gregtech.api.util;

import gregtech.api.pattern.TraceabilityPredicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;

/**
 * BlockInfo represents immutable information for block in world
 * This includes block state and tile entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    private final IBlockState blockState;
    private final TileEntity tileEntity;
    private final TraceabilityPredicate predicate;
    private boolean manualFacing;

    public BlockInfo(Block block) {
        this(block.getDefaultState());
    }

    public BlockInfo(IBlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity) {
        this(blockState, tileEntity, null);
    }

    public BlockInfo(IBlockState blockState, TileEntity tileEntity, TraceabilityPredicate predicate) {
        // the predicate is an extremely scuffed way of displaying candidates during preview
        // ideally you would bind the predicate to the char in the code but uh yeah
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.predicate = predicate;
        Preconditions.checkArgument(tileEntity == null || blockState.getBlock().hasTileEntity(blockState),
                "Cannot create block info with tile entity for block not having it");
    }

    public BlockInfo manualFacing() {
        this.manualFacing = true;
        return this;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public TraceabilityPredicate getPredicate() {
        return predicate;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlockState(pos, blockState);
        if (tileEntity != null) {
            world.setTileEntity(pos, tileEntity);
        }
    }
}
