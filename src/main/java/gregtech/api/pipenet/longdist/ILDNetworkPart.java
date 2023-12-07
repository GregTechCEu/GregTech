package gregtech.api.pipenet.longdist;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Blocks or {@link ILDEndpoint}'s that can be part of a ld network should implement this interface.
 */
public interface ILDNetworkPart {

    /**
     * @return the long distance pipe type of this part (f.e. item or fluid)
     */
    @NotNull
    LongDistancePipeType getPipeType();

    @Nullable
    static ILDNetworkPart tryGet(World world, BlockPos pos) {
        return tryGet(world, pos, world.getBlockState(pos));
    }

    @Nullable
    static ILDNetworkPart tryGet(World world, BlockPos pos, IBlockState blockState) {
        return blockState.getBlock() instanceof ILDNetworkPart part ? part : ILDEndpoint.tryGet(world, pos);
    }
}
