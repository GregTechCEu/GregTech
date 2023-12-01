package gregtech.api.util;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockUtility {

    private static final BlockWrapper WRAPPER = new BlockWrapper();
    private static final Object2BooleanMap<IBlockState> ORE_CACHE = new Object2BooleanOpenHashMap<>();

    public static void startCaptureDrops() {
        WRAPPER.captureDrops(true);
    }

    @NotNull
    public static NonNullList<ItemStack> stopCaptureDrops() {
        return WRAPPER.captureDrops(false);
    }

    public static boolean isOre(@NotNull IBlockState state) {
        return ORE_CACHE.computeIfAbsent(Objects.requireNonNull(state, "state == null"), s -> {
            Item item = Item.getItemFromBlock(s.getBlock());
            int meta = s.getBlock().getMetaFromState(s);
            OrePrefix orePrefix = OreDictUnifier.getPrefix(item, meta);
            return orePrefix != null && orePrefix.name().startsWith("ore");
        });
    }

    /**
     * Mark a block state as an ore / not an ore, for miners and prospectors.
     *
     * @param state A block state
     * @param isOre Whether this block state is an ore or not
     * @throws NullPointerException if {@code state == null}
     */
    public static void markBlockstateAsOre(@NotNull IBlockState state, boolean isOre) {
        ORE_CACHE.put(Objects.requireNonNull(state, "state == null"), isOre);
    }

    private static class BlockWrapper extends Block {

        public BlockWrapper() {
            super(Material.AIR);
        }

        @NotNull
        @Override
        public NonNullList<ItemStack> captureDrops(boolean start) {
            return super.captureDrops(start);
        }
    }
}
