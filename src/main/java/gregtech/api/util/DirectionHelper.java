package gregtech.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class DirectionHelper {

    private DirectionHelper() {
    }

    public static EnumFacing getRelativeUp(EntityPlayer player) {
        return getRelativeDown(player).getOpposite();
    }

    public static EnumFacing getRelativeDown(EntityPlayer player) {
        Vec3d facingVec = player.getLookVec();
        EnumFacing facing = EnumFacing.getFacingFromVector((float) facingVec.x, (float) facingVec.y, (float) facingVec.z);

        EnumFacing down;
        switch (facing) {
            case UP:
                down = player.getHorizontalFacing(); break;
            case DOWN:
                down = player.getHorizontalFacing().getOpposite(); break;
            default:
                down = EnumFacing.DOWN;
        }
        return down;
    }

    public static EnumFacing getRelativeLeft(EntityPlayer player) {
        return getRelativeRight(player).getOpposite();
    }

    public static EnumFacing getRelativeRight(EntityPlayer player) {
        Vec3d facingVec = player.getLookVec();
        EnumFacing facing = EnumFacing.getFacingFromVector((float) facingVec.x, (float) facingVec.y, (float) facingVec.z);

        EnumFacing right;
        switch (facing) {
            case UP:
            case DOWN:
                facing = player.getHorizontalFacing();
            default:
                right = facing.rotateAround(EnumFacing.Axis.Y);
        }
        return right;
    }

    public static EnumFacing getRelativeForward(EntityPlayer player) {
        Vec3d facingVec = player.getLookVec();
        return EnumFacing.getFacingFromVector((float) facingVec.x, (float) facingVec.y, (float) facingVec.z);
    }

    public static EnumFacing getRelativeBackward(EntityPlayer player) {
        return getRelativeForward(player).getOpposite();
    }

    public static Vec3i multiplyVec(Vec3i start, int multiplier) {
        return new Vec3i(
                start.getX() * multiplier,
                start.getY() * multiplier,
                start.getZ() * multiplier
        );
    }
}
