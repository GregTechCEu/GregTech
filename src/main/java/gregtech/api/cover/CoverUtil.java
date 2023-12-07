package gregtech.api.cover;

import net.minecraft.util.EnumFacing;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CoverUtil {

    public static Cuboid6 getCoverPlateBox(@NotNull EnumFacing side, double plateThickness) {
        return switch (side) {
            case UP -> new Cuboid6(0.0, 1.0 - plateThickness, 0.0, 1.0, 1.0, 1.0);
            case DOWN -> new Cuboid6(0.0, 0.0, 0.0, 1.0, plateThickness, 1.0);
            case NORTH -> new Cuboid6(0.0, 0.0, 0.0, 1.0, 1.0, plateThickness);
            case SOUTH -> new Cuboid6(0.0, 0.0, 1.0 - plateThickness, 1.0, 1.0, 1.0);
            case WEST -> new Cuboid6(0.0, 0.0, 0.0, plateThickness, 1.0, 1.0);
            case EAST -> new Cuboid6(1.0 - plateThickness, 0.0, 0.0, 1.0, 1.0, 1.0);
        };
    }

    public static boolean doesCoverCollide(@NotNull EnumFacing side, @NotNull List<IndexedCuboid6> collisionBox,
                                           double plateThickness) {
        if (plateThickness > 0.0) {
            Cuboid6 coverPlateBox = getCoverPlateBox(side, plateThickness);
            for (Cuboid6 collisionCuboid : collisionBox) {
                if (collisionCuboid.intersects(coverPlateBox)) {
                    // collision box intersects with machine bounding box, so the cover cannot be placed on this side
                    return true;
                }
            }
        }
        return false;
    }
}
