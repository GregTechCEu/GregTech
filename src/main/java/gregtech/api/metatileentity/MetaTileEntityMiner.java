package gregtech.api.metatileentity;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class MetaTileEntityMiner extends MetaTileEntity {

    public MetaTileEntityMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    abstract public int getTicksPerOperation();
    abstract public int getChunkRange();
    abstract public int getFortune();
    abstract public int getDrillingFluidConsumedPerTick();

    public long getNbBlock() {
        return 1L;
    }

    static List<BlockPos> getBlockToMinePerChunk(MetaTileEntityMiner miner, AtomicLong x, AtomicLong y, AtomicLong z, ChunkPos chunkPos) {
        List<BlockPos> blocks = new ArrayList<>();
        for (int i = 0; i < miner.getNbBlock(); i++) {
            if (y.get() >= 0 && miner.getOffsetTimer() % miner.getTicksPerOperation() == 0) {
                if (z.get() <= chunkPos.getZEnd()) {
                    if (x.get() <= chunkPos.getXEnd()) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        Block block = miner.getWorld().getBlockState(blockPos).getBlock();
                        if (miner.getWorld().getTileEntity(blockPos) == null) {
                            if (isOre(block)) {
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
        return blocks;
    }

    static boolean isOre(Block block) {
        OrePrefix orePrefix = OreDictUnifier.getPrefix(new ItemStack(block));
        return orePrefix != null && orePrefix.name().startsWith("ore");
    }

}
