package gregtech.api.util;

import gregtech.api.pattern.GreggyBlockPos;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.function.BinaryOperator;
import java.util.function.ToIntFunction;

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
        return VALUES[ordinal() ^ 1];
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

    public ToIntFunction<BlockPos> getSorter(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
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
        EnumFacing cross = GTUtility.cross(newFrontFacing, oldFrontFacing);
        if (cross == null) return upwardsFacing;
        if (cross.getAxis() == upwardsFacing.getAxis()) return upwardsFacing;
        if (upwardsFacing == newFrontFacing) return oldFrontFacing.getOpposite();
        return oldFrontFacing;
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos offsetPos(BlockPos pos, EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        GreggyBlockPos greg = new GreggyBlockPos(pos);
        greg.offset(UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), upOffset);
        greg.offset(LEFT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), leftOffset);
        greg.offset(FRONT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), forwardOffset);
        return greg.immutable();
    }
}
