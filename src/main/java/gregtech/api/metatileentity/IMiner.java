package gregtech.api.metatileentity;


import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedList;
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

    Type getType();

    World getWorld();

    long getOffsetTimer();

    static LinkedList<BlockPos> getBlocksToMine(IMiner miner, AtomicInteger x, AtomicInteger y, AtomicInteger z, AtomicInteger startX, AtomicInteger startZ, AtomicInteger startY, int aRadius) {
        LinkedList<BlockPos> blocks = new LinkedList<>();
        while (y.get() > 0) {
            if (z.get() <= startZ.get() + aRadius * 2) {
                if (x.get() <= startX.get() + aRadius * 2) {
                    LinkedList<BlockPos> blockPos = new LinkedList<>();
                    for (int a = 0; a < aRadius * 2; a++) {
                        blockPos.addLast(new BlockPos(x.get(), y.get(), startZ.get() + a));
                        Block block = miner.getWorld().getBlockState(blockPos.getLast()).getBlock();
                        if (miner.getWorld().getTileEntity(blockPos.getLast()) == null && GTUtility.isOre(block)) {
                            blocks.addLast(blockPos.getLast());
                        }
                    }
                    x.incrementAndGet();
                } else {
                    x.set(x.get() - aRadius * 2);
                    z.set(z.get() + aRadius * 2 + 1);
                }
            } else {
                z.set(z.get() - aRadius * 2);
                y.decrementAndGet();
            }
        }
        return blocks;
    }
}
