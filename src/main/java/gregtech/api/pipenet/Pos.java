package gregtech.api.pipenet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class Pos {

    private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public static long asLong(Vec3i vec3i) {
        return asLong(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long asLong(int x, int y, int z) {
        return (x & X_MASK) << X_SHIFT | (y & Y_MASK) << Y_SHIFT | (z & Z_MASK) << 0;
    }

    /**
     * Create a BlockPos from a serialized long value (created by toLong)
     */
    public static BlockPos fromLong(long serialized) {
        int i = (int)(serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int j = (int)(serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int k = (int)(serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new BlockPos(i, j, k);
    }

    public static void setPos(BlockPos.MutableBlockPos pos, long rawPos) {
        pos.setPos(getX(rawPos), getY(rawPos), getZ(rawPos));
    }

    public static int getX(long pos) {
        return (int)(pos << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
    }

    public static int getY(long pos) {
        return (int)(pos << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
    }

    public static int getZ(long pos) {
        return (int)(pos << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
    }

}
