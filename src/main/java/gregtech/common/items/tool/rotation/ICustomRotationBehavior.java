package gregtech.common.items.tool.rotation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICustomRotationBehavior {

    /** Whether this custom rotation behavior is applicable to the provided Block. */
    boolean doesApply(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos);

    /**
     * Custom implementation of {@link Block#rotateBlock(World, BlockPos, EnumFacing)} for when that behavior isn't
     * ideal.
     */
    boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult);

    /** Whether to show the 9-sectioned highlight grid when looking at this block while holding a Wrench. */
    default boolean showGrid() {
        return true;
    }

    /** Whether to draw an X on a provided side in the 9-sectioned highlight grid. */
    default boolean showXOnSide(IBlockState state, World world, BlockPos pos, EnumFacing facing) {
        return false;
    }

    /**
     * Whether this custom rotation behavior has some sort of additional spin behavior.
     * You should implement this spin behavior in {@link #customRotate}, this method
     * is only here for the block highlight render.
     */
    default boolean allowSpin() {
        return false;
    }

    /**
     * If your rotation behavior allows spin, provide the front facing of your block here.
     * This is the face that the spin icon will be applied to.
     */
    @Nullable
    default EnumFacing getSpinFrontFacing(IBlockState state, World world, BlockPos pos) {
        return null;
    }
}
