package gregtech.worldgen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface WorldgenPlaceable {

    /**
     * Place in world as a regular ore
     *
     * @param world    the world to place in
     * @param pos      the position to place at
     * @param existing the state currently at the position
     */
    void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing);

    /**
     * Place in world as a small ore
     *
     * @param world    the world to place in
     * @param pos      the position to place at
     * @param existing the state currently at the position
     */
    void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing);
}
