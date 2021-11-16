package gregtech.api.metatileentity;


import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface IMiner {

    short MAX_SPEED = Short.MAX_VALUE;

    byte POWER = 5;

    byte TICK_TOLERANCE = 20;

    double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    static double GET_QUOTIENT(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    World getWorld();

    long getOffsetTimer();

    int getTick();

    int getCurrentRadius();

    int getFortune();

    boolean testForMax();

    void initPos();

    static LinkedList<BlockPos> getBlocksToMine(IMiner miner, AtomicInteger x, AtomicInteger y, AtomicInteger z, AtomicInteger startX, AtomicInteger startZ, int aRadius, double tps) {
        LinkedList<BlockPos> blocks = new LinkedList<>();
        int calcAmount = GET_QUOTIENT(tps) < 1 ? 1 : (int) (Math.min(GET_QUOTIENT(tps), Short.MAX_VALUE));
        for (int c = 0; c < calcAmount; ) {
            if (y.get() > 0) {
                if (z.get() <= startZ.get() + aRadius * 2) {
                    if (x.get() <= startX.get() + aRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        Block block = miner.getWorld().getBlockState(blockPos).getBlock();
                        if (block.blockHardness >= 0 && miner.getWorld().getTileEntity(blockPos) == null && GTUtility.isOre(block)) {
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

    static double getMeanTickTime(World world) {
        return mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void applyTieredHammerNoRandomDrops(Random random, IBlockState blockState, List<ItemStack> drops, int fortuneLevel, EntityPlayer player, RecipeMap map, int tier) {
        ItemStack itemStack = new ItemStack(blockState.getBlock(), 1, blockState.getBlock().getMetaFromState(blockState));
        Recipe recipe = map.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0, MatchingMode.IGNORE_FLUIDS);
        if (recipe != null && !recipe.getOutputs().isEmpty()) {
            drops.clear();
            for (ItemStack outputStack : recipe.getResultItemOutputs(Integer.MAX_VALUE, random, tier)) {
                outputStack = outputStack.copy();
                if (!(player instanceof FakePlayer) && OreDictUnifier.getPrefix(outputStack) == OrePrefix.crushed) {
                    if (fortuneLevel > 0) {
                        int i = Math.max(0, fortuneLevel - 1);
                        outputStack.grow(outputStack.getCount() * i);
                    }
                }
                drops.add(outputStack);
            }
        }
    }
}
