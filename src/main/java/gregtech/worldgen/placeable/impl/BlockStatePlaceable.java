package gregtech.worldgen.placeable.impl;

import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockStatePlaceable implements WorldgenPlaceable {

    private final IBlockState state;
    private final boolean placeRegular;
    private final boolean placeSmall;
    private final boolean placeIndicator;

    /**
     * @param state the state to place
     * @param placeRegular if the state should be placed for regular placement
     * @param placeSmall if the state should be placed for small placement
     * @param placeIndicator if the state should be placed for indicator placement
     */
    public BlockStatePlaceable(@NotNull IBlockState state, boolean placeRegular, boolean placeSmall, boolean placeIndicator) {
        this.state = state;
        this.placeRegular = placeRegular;
        this.placeSmall = placeSmall;
        this.placeIndicator = placeIndicator;
    }

    @Override
    public void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        if (hasRegular()) {
            world.setBlockState(pos, state, 16); // prevent observer updates with flag 16
        }
    }

    @Override
    public boolean hasRegular() {
        return placeRegular;
    }

    @Override
    public void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        if (hasSmall()) {
            place(world, pos, existing);
        }
    }

    @Override
    public boolean hasSmall() {
        return placeSmall;
    }

    @Override
    public void placeIndicator(@NotNull World world, @NotNull BlockPos pos) {
        if (hasIndicator()) {
            //noinspection DataFlowIssue
            place(world, pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public boolean hasIndicator() {
        return placeIndicator;
    }
}
