package gregtech.api.metatileentity;


import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public interface IMiner {


    enum Type {
        STEAM(320, 9, 0, "", 0),
        LV(160, 17, 0, "", 0),
        MV(80, 33, 0, "", 0),
        HV(40, 49, 0, "", 0),
        BASIC(16, 3, 3, GTUtility.romanNumeralString(3), 8),
        LARGE(4, 5, 3, GTUtility.romanNumeralString(3), 16),
        ADVANCE(1, 7, 3, GTUtility.romanNumeralString(3), 32);

        public final int tick;
        public final int radius;
        public final int fortune;
        public final int drillingFluidConsumePerTick;
        public final String fortuneString;


        Type(int tick, int radius, int fortune, String fortuneString, int drillingFluidConsumePerTick) {
            this.tick = tick;
            this.radius = radius;
            this.fortune = fortune;
            this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
            this.fortuneString = fortuneString;
        }

    }

    short MAX_SPEED = 256;

    byte POWER = 4;

    byte TICK_TOLERANCE = 30;

    double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    static double GET_QUOTIENT(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    Type getType();

    World getWorld();

    long getOffsetTimer();

    static LinkedList<BlockPos> getBlocksToMine(IMiner miner, AtomicInteger x, AtomicInteger y, AtomicInteger z, AtomicInteger startX, AtomicInteger startZ, AtomicInteger startY, int aRadius, double tps) {
        LinkedList<BlockPos> blocks = new LinkedList<>();
        int calcAmount = GET_QUOTIENT(tps) < 1 ? 1 : (int) (Math.min(GET_QUOTIENT(tps), Short.MAX_VALUE));
        for (int c = 0; c < calcAmount; ) {
            if (y.get() > 0) {
                if (z.get() <= startZ.get() + aRadius * 2) {
                    if (x.get() <= startX.get() + aRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        Block block = miner.getWorld().getBlockState(blockPos).getBlock();
                        if (miner.getWorld().getTileEntity(blockPos) == null && GTUtility.isOre(block)) {
                            blocks.addLast(blockPos);
                        }
                        x.incrementAndGet();
                    } else {
                        x.set(x.get() - aRadius * 2);
                        z.incrementAndGet();
                    }
                } else {
                    z.set(z.get() - aRadius * 2);
                    y.decrementAndGet();
                }
            } else
                return blocks;
            if (!blocks.isEmpty())
                c++;
        }
        return blocks;
    }

    static long mean(long[] values) {
        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    static double getTPS(World world) {
        double meanTickTime = mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
        return meanTickTime;
    }
}
