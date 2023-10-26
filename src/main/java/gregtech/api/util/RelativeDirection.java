package gregtech.api.util;

import net.minecraft.util.EnumFacing;
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

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upwardsFacing) {
        return switch (this) {
            case UP -> {
                if (frontFacing == EnumFacing.UP || frontFacing == EnumFacing.DOWN) {
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
                if (frontFacing == EnumFacing.UP || frontFacing == EnumFacing.DOWN) {
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
                if (frontFacing == EnumFacing.UP || frontFacing == EnumFacing.DOWN) {
                    yield upwardsFacing.rotateY();
                } else {
                    yield switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateYCCW();
                        case SOUTH -> frontFacing.rotateY();
                        case EAST -> EnumFacing.DOWN;
                        default -> EnumFacing.UP; // WEST
                    };
                }
            }
            case RIGHT -> {
                if (frontFacing == EnumFacing.UP || frontFacing == EnumFacing.DOWN) {
                    yield upwardsFacing.rotateYCCW();
                } else {
                    yield switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateY();
                        case SOUTH -> frontFacing.rotateYCCW();
                        case EAST -> EnumFacing.UP;
                        default -> EnumFacing.DOWN; // WEST
                    };
                }
            }
            // same direction as front facing, upwards facing doesn't matter
            case FRONT -> frontFacing;
            // opposite direction as front facing, upwards facing doesn't matter
            case BACK -> frontFacing.getOpposite();
        };
    }

    public Function<BlockPos, Integer> getSorter(EnumFacing frontFacing, EnumFacing upwardsFacing) {
        // get the direction to go in for the part sorter
        EnumFacing sorterDirection = getRelativeFacing(frontFacing, upwardsFacing);

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
}
