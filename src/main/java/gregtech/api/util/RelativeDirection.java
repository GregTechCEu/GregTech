package gregtech.api.util;

import net.minecraft.util.EnumFacing;
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
        return ordinal() ^ 1;
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upFacing) {
        if (frontFacing.getAxis() == upFacing.getAxis()) {
            throw new IllegalArgumentException("Front facing and up facing must be on different axes!");
        }
        return facingFunction.apply(frontFacing, upFacing);
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        return (isFlipped && (this == LEFT || this == RIGHT)) ?
                getRelativeFacing(frontFacing, upwardsFacing).getOpposite() :
                getRelativeFacing(frontFacing, upwardsFacing);
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
}
