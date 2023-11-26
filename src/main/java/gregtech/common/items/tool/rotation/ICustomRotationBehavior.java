package gregtech.common.items.tool.rotation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface ICustomRotationBehavior {

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
    default boolean showXOnSide(IBlockState state, EnumFacing facing) {
        return false;
    }
}
