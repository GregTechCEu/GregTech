package gregtech.worldgen.placeable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class EmptyPlaceable implements WorldgenPlaceable {

    public static final EmptyPlaceable INSTANCE = new EmptyPlaceable();

    private EmptyPlaceable() {}

    @Override
    public void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {}

    @Override
    public boolean hasRegular() {
        return false;
    }

    @Override
    public void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {}

    @Override
    public boolean hasSmall() {
        return false;
    }

    @Override
    public void placeIndicator(@NotNull World world, @NotNull BlockPos pos) {}

    @Override
    public boolean hasIndicator() {
        return false;
    }
}
