package gregtech.common.items.tool.rotation;

import gregtech.api.cover.CoverRayTracer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AECustomBlockRotations {

    static void init() {
        ICustomRotationBehavior aeOrientableBehavior = new ICustomRotationBehavior() {

            @Override
            public boolean doesApply(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos) {
                if (!state.isFullBlock()) return false; // try to exclude some weird stuff
                IOrientable orientable = getOrientable(world, pos);
                return orientable != null && orientable.canBeRotated();
            }

            @Override
            public boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult) {
                IOrientable orientable = getOrientable(world, pos);
                if (orientable == null) return false;

                EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
                if (gridSide == null) return false;

                if (gridSide == orientable.getForward()) {
                    // spin
                    Axis frontAxis = orientable.getForward().getAxis();
                    EnumFacing newFacing = orientable.getUp().rotateAround(frontAxis);
                    if (orientable.getForward().getAxisDirection() == AxisDirection.NEGATIVE) {
                        newFacing = newFacing.getOpposite();
                    }
                    orientable.setOrientation(orientable.getForward(), newFacing);
                } else {
                    // rotate
                    EnumFacing newUpwardsFacing = simulateAxisRotation(gridSide,
                            orientable.getForward(), orientable.getUp());
                    orientable.setOrientation(gridSide, newUpwardsFacing);
                }
                return true;
            }

            @Override
            public boolean allowSpin() {
                return true;
            }

            @Override
            public @Nullable EnumFacing getSpinFrontFacing(IBlockState state, World world, BlockPos pos) {
                IOrientable orientable = getOrientable(world, pos);
                if (orientable == null) return null;
                return orientable.getForward();
            }

            private IOrientable getOrientable(World world, BlockPos pos) {
                if (world.getTileEntity(pos) instanceof IOrientable orientable) {
                    return orientable;
                }
                return null;
            }
        };

        CustomBlockRotations.registerCustomRotation(aeOrientableBehavior);
    }

    /* Similar to the one in RelativeDirection, but AE stores their upwards facing as absolute instead of relative */
    private static EnumFacing simulateAxisRotation(EnumFacing newFrontFacing, EnumFacing oldFrontFacing,
                                                   EnumFacing upwardsFacing) {
        if (newFrontFacing == oldFrontFacing) return upwardsFacing;

        Axis newAxis = newFrontFacing.getAxis();
        Axis oldAxis = oldFrontFacing.getAxis();

        if (newAxis != Axis.Y && oldAxis != Axis.Y) {
            // was on horizontal axis and still is
            EnumFacing newUpwardsFacing = upwardsFacing;
            if (oldFrontFacing.rotateY() == upwardsFacing) {
                // upwards facing is left
                newUpwardsFacing = newFrontFacing.rotateY();
            } else if (oldFrontFacing.rotateYCCW() == upwardsFacing) {
                // upwards facing is right
                newUpwardsFacing = newFrontFacing.rotateYCCW();
            }
            return newUpwardsFacing;
        } else if (newAxis == Axis.Y && oldAxis != Axis.Y) {
            // going from horizontal to vertical axis
            EnumFacing newUpwardsFacing = upwardsFacing;
            if (upwardsFacing == EnumFacing.UP) {
                newUpwardsFacing = oldFrontFacing.getOpposite();
            } else if (upwardsFacing == EnumFacing.DOWN) {
                newUpwardsFacing = oldFrontFacing;
            }
            return newUpwardsFacing;
        } else if (newAxis != Axis.Y) {
            // going from vertical to horizontal axis
            EnumFacing newUpwardsFacing = upwardsFacing;
            if (newFrontFacing == upwardsFacing) {
                newUpwardsFacing = EnumFacing.DOWN;
            } else if (newFrontFacing == upwardsFacing.getOpposite()) {
                newUpwardsFacing = EnumFacing.UP;
            }
            return newUpwardsFacing;
        } else {
            // was on vertical axis and still is
            return upwardsFacing.getOpposite();
        }
    }
}
