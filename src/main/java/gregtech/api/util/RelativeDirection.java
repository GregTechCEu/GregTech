package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {

    UP(f -> EnumFacing.UP),
    DOWN(f -> EnumFacing.DOWN),
    LEFT(EnumFacing::rotateYCCW),
    RIGHT(EnumFacing::rotateY),
    FRONT(Function.identity()),
    BACK(EnumFacing::getOpposite);

    final Function<EnumFacing, EnumFacing> actualFacing;

    RelativeDirection(Function<EnumFacing, EnumFacing> actualFacing) {
        this.actualFacing = actualFacing;
    }

    public EnumFacing getActualFacing(EnumFacing facing) {
        return actualFacing.apply(facing);
    }

    public EnumFacing apply(EnumFacing facing) {
        return actualFacing.apply(facing);
    }

    public Vec3i applyVec3i(EnumFacing facing) {
        return apply(facing).getDirectionVec();
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        EnumFacing.Axis frontAxis = frontFacing.getAxis();
        return switch (this) {
            case UP -> {
                if (frontAxis == Axis.Y) {
                    // same direction as upwards facing
                    yield upwardsFacing;
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> EnumFacing.UP;
                        case SOUTH -> EnumFacing.DOWN;
                        case EAST -> frontFacing.rotateYCCW();
                        default -> frontFacing.rotateY(); // WEST
                    };
                }
            }
            case DOWN -> {
                if (frontAxis == Axis.Y) {
                    // opposite direction as upwards facing
                    yield upwardsFacing.getOpposite();
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> EnumFacing.DOWN;
                        case SOUTH -> EnumFacing.UP;
                        case EAST -> frontFacing.rotateY();
                        default -> frontFacing.rotateYCCW(); // WEST
                    };
                }
            }
            case LEFT -> {
                EnumFacing facing;
                if (frontAxis == Axis.Y) {
                    facing = upwardsFacing.rotateY();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateYCCW();
                        case SOUTH -> frontFacing.rotateY();
                        case EAST -> EnumFacing.DOWN;
                        default -> EnumFacing.UP; // WEST
                    };
                }
                yield isFlipped ? facing.getOpposite() : facing;
            }
            case RIGHT -> {
                EnumFacing facing;
                if (frontAxis == Axis.Y) {
                    facing = upwardsFacing.rotateYCCW();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateY();
                        case SOUTH -> frontFacing.rotateYCCW();
                        case EAST -> EnumFacing.UP;
                        default -> EnumFacing.DOWN; // WEST
                    };
                }
                // invert if flipped
                yield isFlipped ? facing.getOpposite() : facing;
            }
            // same direction as front facing, upwards facing doesn't matter
            case FRONT -> frontFacing;
            // opposite direction as front facing, upwards facing doesn't matter
            case BACK -> frontFacing.getOpposite();
        };
    }

    public Function<BlockPos, Integer> getSorter(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        // get the direction to go in for the part sorter
        EnumFacing sorterDirection = getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

        // Determined by EnumFacing Axis + AxisDirection
        return switch (sorterDirection) {
            case UP -> BlockPos::getY;
            case DOWN -> pos -> -pos.getY();
            case EAST -> BlockPos::getX;
            case WEST -> pos -> -pos.getX();
            case NORTH -> pos -> -pos.getZ();
            case SOUTH -> BlockPos::getZ;
        };
    }

    /**
     * Simulates rotating the controller around an axis to get to a new front facing.
     *
     * @return Returns the new upwards facing.
     */
    public static EnumFacing simulateAxisRotation(EnumFacing newFrontFacing, EnumFacing oldFrontFacing,
                                                  EnumFacing upwardsFacing) {
        if (newFrontFacing == oldFrontFacing) return upwardsFacing;

        EnumFacing.Axis newAxis = newFrontFacing.getAxis();
        EnumFacing.Axis oldAxis = oldFrontFacing.getAxis();

        if (newAxis != Axis.Y && oldAxis != Axis.Y) {
            // no change needed
            return upwardsFacing;
        } else if (newAxis == Axis.Y && oldAxis != Axis.Y) {
            // going from horizontal to vertical axis
            EnumFacing newUpwardsFacing = switch (upwardsFacing) {
                case NORTH -> oldFrontFacing.getOpposite();
                case SOUTH -> oldFrontFacing;
                case EAST -> oldFrontFacing.rotateYCCW();
                default -> oldFrontFacing.rotateY(); // WEST
            };
            return newFrontFacing == EnumFacing.DOWN && upwardsFacing.getAxis() == Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else if (newAxis != Axis.Y) {
            // going from vertical to horizontal axis
            EnumFacing newUpwardsFacing;
            if (upwardsFacing == newFrontFacing.getOpposite()) {
                newUpwardsFacing = EnumFacing.NORTH;
            } else if (upwardsFacing == newFrontFacing) {
                newUpwardsFacing = EnumFacing.SOUTH;
            } else if (upwardsFacing == newFrontFacing.rotateY()) {
                newUpwardsFacing = EnumFacing.WEST;
            } else { // rotateYCCW
                newUpwardsFacing = EnumFacing.EAST;
            }
            return oldFrontFacing == EnumFacing.DOWN && newUpwardsFacing.getAxis() == Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else {
            // was on vertical axis and still is. Must have flipped from up to down or vice versa
            return upwardsFacing.getOpposite();
        }
    }
}
