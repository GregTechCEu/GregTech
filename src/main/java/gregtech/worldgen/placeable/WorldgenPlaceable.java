package gregtech.worldgen.placeable;

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
     * @return if the placeable has a regular block to place at all
     */
    boolean hasRegular();

    /**
     * Place in world as a small ore
     *
     * @param world    the world to place in
     * @param pos      the position to place at
     * @param existing the state currently at the position
     */
    void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing);

    /**
     * @return if the placeable has a small block to place at all
     */
    boolean hasSmall();

    /**
     * Place in world as an indicator
     *
     * @param world    the world to place in
     * @param pos      the position to place at
     */
    void placeIndicator(@NotNull World world, @NotNull BlockPos pos);

    /**
     * @return if the placeable has an indicator to place at all
     */
    boolean hasIndicator();
}
