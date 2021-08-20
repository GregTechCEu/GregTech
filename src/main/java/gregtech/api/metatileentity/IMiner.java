package gregtech.api.metatileentity;

import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public interface IMiner {

    int getTicksPerOperation();
    int getChunkRange();
    int getFortune();
    int getDrillingFluidConsumedPerTick();

    default long getNbBlock() {
        return 1L;
    }

    static List<BlockPos> getBlockToMinePerChunk(IMiner miner, AtomicLong x, AtomicLong y, AtomicLong z, ChunkPos chunkPos) {
        List<BlockPos> blocks = new ArrayList<>();
        if ((miner instanceof MetaTileEntity)) {
            MetaTileEntity mteMiner = (MetaTileEntity) miner;
            for (int i = 0; i < miner.getNbBlock(); i++) {
                if (y.get() >= 0 && mteMiner.getOffsetTimer() % miner.getTicksPerOperation() == 0) {
                    if (z.get() <= chunkPos.getZEnd()) {
                        if (x.get() <= chunkPos.getXEnd()) {
                            BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                            Block block = mteMiner.getWorld().getBlockState(blockPos).getBlock();
                            if (mteMiner.getWorld().getTileEntity(blockPos) == null) {
                                if (GTUtility.isOre(block)) {
                                    blocks.add(blockPos);
                                }
                            }
                            x.incrementAndGet();
                        } else {
                            x.set(chunkPos.getXStart());
                            z.incrementAndGet();
                        }
                    } else {
                        z.set(chunkPos.getZStart());
                        y.decrementAndGet();
                    }
                }
            }
        }
        return blocks;
    }

}
