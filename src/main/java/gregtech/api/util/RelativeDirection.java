package gregtech.api.util;

import net.minecraft.util.EnumFacing;
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
}
