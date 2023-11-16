package gregtech.common.metatileentities.miner;

import gregtech.client.model.miningpipe.MiningPipeModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    /**
     * Try to collect drops from the block. {@code true} is returned if the block drop is successfully collected;
     * {@code false} means the operation cannot be done (ex. not enough inventory space to store the drops). Returning
     * {@code false} will momentarily halt the miner operation.
     *
     * @param world the {@link World} the miner is in
     * @param pos   the {@link BlockPos} of the block being mined
     * @param state the {@link IBlockState} of the block being mined
     * @return Whether the action was successful
     */
    boolean collectBlockDrops(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state);

    @Nonnull
    @SideOnly(Side.CLIENT)
    MiningPipeModel getMiningPipeModel();

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
        BLOCK
    }
}
