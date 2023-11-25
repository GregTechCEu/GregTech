package gregtech.worldgen.placeable;

import gregtech.worldgen.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockStatePlaceable implements WorldgenPlaceable {

    private final IBlockState state;
    private final boolean placeAsSmall;

    /**
     * @param state the state to place
     */
    public BlockStatePlaceable(@NotNull IBlockState state) {
        this(state, false);
    }

    /**
     * @param state the state to place
     * @param placeAsSmall if the state should also be placed for small placement
     */
    public BlockStatePlaceable(@NotNull IBlockState state, boolean placeAsSmall) {
        this.state = state;
        this.placeAsSmall = placeAsSmall;
    }

    @Override
    public void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        world.setBlockState(pos, state, 0);
    }

    @Override
    public void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        if (placeAsSmall) {
            place(world, pos, existing);
        }
    }
}
