package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {

    UP((f, u) -> u),
    DOWN((f, u) -> u.getOpposite()),
    LEFT((f, u) -> GTUtility.cross(f, u).getOpposite()),
    RIGHT(GTUtility::cross),
    FRONT((f, u) -> f),
    BACK((f, u) -> f.getOpposite());

    final BinaryOperator<EnumFacing> facingFunction;

    /**
     * do not mutate this unless you want your house to explode
     */
    public static final RelativeDirection[] VALUES = values();

    RelativeDirection(BinaryOperator<EnumFacing> facingFunction) {
        this.facingFunction = facingFunction;
    }

    /**
     * Gets the opposite RelativeDirection to this. UP <-> DOWN, LEFT <-> RIGHT, FRONT <-> BACK.
     */
    public RelativeDirection getOpposite() {
        return VALUES[oppositeOrdinal()];
    }

    /**
     * Gets the ordinal of the RelativeDirection opposite to this. UP <-> DOWN, LEFT <-> RIGHT, FRONT <-> BACK.
     */
    public int oppositeOrdinal() {
        // floor to nearest even + adjustment
        return (ordinal() / 2) * 2 + (1 - ordinal() % 2);
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upFacing) {
        if (frontFacing.getAxis() == upFacing.getAxis()) {
            throw new IllegalArgumentException("Front facing and up facing must be on different axes!");
        }
        return facingFunction.apply(frontFacing, upFacing);
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        return (isFlipped && (this == LEFT || this == RIGHT)) ? getRelativeFacing(frontFacing, upwardsFacing).getOpposite() : getRelativeFacing(frontFacing, upwardsFacing);
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
        // 180 degree flip
        if (newFrontFacing.getAxis() == oldFrontFacing.getAxis()) return upwardsFacing;

        EnumFacing cross = GTUtility.cross(newFrontFacing, oldFrontFacing);

        if (cross.getAxis() == upwardsFacing.getAxis()) return upwardsFacing;

        // clockwise rotation
        if (oldFrontFacing.rotateAround(cross.getAxis()) == newFrontFacing) {
            return oldFrontFacing.getOpposite();
        }

        return oldFrontFacing;
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    // todo rework/remove this also
    public static BlockPos offsetPos(BlockPos pos, EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        if (upOffset == 0 && leftOffset == 0 && forwardOffset == 0) {
            return pos;
        }

        int oX = 0, oY = 0, oZ = 0;
        final EnumFacing relUp = UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relUp.getXOffset() * upOffset;
        oY += relUp.getYOffset() * upOffset;
        oZ += relUp.getZOffset() * upOffset;

        final EnumFacing relLeft = LEFT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relLeft.getXOffset() * leftOffset;
        oY += relLeft.getYOffset() * leftOffset;
        oZ += relLeft.getZOffset() * leftOffset;

        final EnumFacing relForward = FRONT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relForward.getXOffset() * forwardOffset;
        oY += relForward.getYOffset() * forwardOffset;
        oZ += relForward.getZOffset() * forwardOffset;

        return pos.add(oX, oY, oZ);
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos setActualRelativeOffset(int x, int y, int z, EnumFacing facing, EnumFacing upwardsFacing,
                                                   boolean isFlipped, RelativeDirection[] structureDir) {
        int[] c0 = new int[] { x, y, z }, c1 = new int[3];
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            EnumFacing of = facing == EnumFacing.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(of)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            int xOffset = upwardsFacing.getXOffset();
            int zOffset = upwardsFacing.getZOffset();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    c1[0] = -c1[0]; // flip X-axis
                } else {
                    c1[2] = -c1[2]; // flip Z-axis
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(facing)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            if (upwardsFacing == EnumFacing.WEST || upwardsFacing == EnumFacing.EAST) {
                int xOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getXOffset() :
                        facing.rotateY().getOpposite().getXOffset();
                int zOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getZOffset() :
                        facing.rotateY().getOpposite().getZOffset();
                int tmp;
                if (xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == EnumFacing.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getXOffset() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                        c1[0] = -c1[0]; // flip X-axis
                    } else {
                        c1[2] = -c1[2]; // flip Z-axis
                    }
                } else {
                    c1[1] = -c1[1]; // flip Y-axis
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
