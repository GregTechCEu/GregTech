package gtqt.api.util;

import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Unique;

public class alculateFacing {

    @Unique
    public static float gregTech$normalizeYaw(float yaw) {
        float normalized = yaw % 360.0F;
        if (normalized < 0) {
            normalized += 360.0F;
        }
        return normalized;
    }
    @Unique
    public static EnumFacing gregTech$calculateFacingFromYaw(float yaw) {
        // 使用45度偏移确保边界情况正确处理
        float offsetYaw = (yaw + 45.0F) % 360.0F;

        // 划分四个90度的区间
        if (offsetYaw < 90.0F) {
            return EnumFacing.SOUTH;  // 315°~45° -> SOUTH
        } else if (offsetYaw < 180.0F) {
            return EnumFacing.WEST;   // 45°~135° -> WEST
        } else if (offsetYaw < 270.0F) {
            return EnumFacing.NORTH;  // 135°~225° -> NORTH
        } else {
            return EnumFacing.EAST;   // 225°~315° -> EAST
        }
    }

}
