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
}
