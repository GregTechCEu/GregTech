package gregtech.common.metatileentities.miner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;

public interface Miner {

    /**
     * Try to drain all mining resources required for one operation. (e.g. energy, mining fluids)
     *
     * @param minedBlockType Type of the block mined
     * @param pipeExtended   Whether the pipe length got extended
     * @param simulate       If {@code true}, this action will not affect the state of the game
     * @return Whether the action was successful
     */
    boolean drainMiningResources(@Nonnull MinedBlockType minedBlockType, boolean pipeExtended, boolean simulate);

    default boolean canOperate() {
        return true;
    }

    /**
     * Called after each block is mined.
     *
     * @param pos      position of the block mined
     * @param isOre    whether it was ore block
     * @param isOrigin whether it was origin (the block mining pipe goes in)
     */
    default void onMineOperation(@Nonnull BlockPos pos, boolean isOre, boolean isOrigin) {}

    /**
     * Called to handle mining regular ores and blocks
     *
     * @param drops the List of items to fill after the operation
     * @param world the {@link WorldServer} the miner is in
     * @param pos   the {@link BlockPos} of the block being mined
     * @param state the {@link IBlockState} of the block being mined
     */
    default void getRegularBlockDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        state.getBlock().getDrops(drops, world, pos, state, 0); // regular ores do not get fortune applied
    }

    /**
     * Type of the block mined.
     */
    enum MinedBlockType {
        /**
         * Mined nothing
         */
        NOTHING,
        /**
         * Mined an ore
         */
        ORE,
        /**
         * Mined a block that isn't an ore, like a block in the center (pipe column).
         */
        BLOCK;
    }
}
