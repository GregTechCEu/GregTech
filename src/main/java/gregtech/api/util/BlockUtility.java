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
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BlockUtility {

    private static final BlockWrapper WRAPPER = new BlockWrapper();
    private static final Object2BooleanMap<IBlockState> ORE_CACHE = new Object2BooleanOpenHashMap<>();
    private static final Object2DoubleMap<IBlockState> walkingSpeedBonusInternal = new Object2DoubleOpenHashMap<>();

    /**
     * View-only collection of block states that give speed bonus when walking over it. The bonus value is a percentage
     * value that gets added to the player speed; for example, a bonus value of {@link 0.25} will add 25% of extra speed
     * to the player.
     */
    public static final Object2DoubleMap<IBlockState> WALKING_SPEED_BONUS = Object2DoubleMaps.unmodifiable(
            walkingSpeedBonusInternal);

    /**
     * UUID of the walking speed bonus attribute applied to players.
     */
    public static final UUID WALKING_SPEED_UUID = UUID.fromString("415ac431-8339-4150-965c-e673a8a328be");

    /**
     * Walking speed bonus applied to asphalt and concrete blocks.
     */
    public static final double ASPHALT_WALKING_SPEED_BONUS = 0.6;
    /**
     * Walking speed bonus applied to studs.
     */
    public static final double STUDS_WALKING_SPEED_BONUS = 0.25;

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

    /**
     * Set walking speed bonus for the block state. The bonus value is a percentage value that gets added to the player
     * speed; for example, a bonus value of {@link 0.25} will add 25% of extra speed to the player.
     *
     * @param state  block state
     * @param amount amount of walking speed bonus
     */
    public static void setWalkingSpeedBonus(@NotNull IBlockState state, double amount) {
        Objects.requireNonNull(state, "state == null");
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Haha funny i put NaN and Infinity in your API method haha no");
        }
        if (amount == 0) {
            walkingSpeedBonusInternal.remove(state);
        } else {
            walkingSpeedBonusInternal.put(state, amount);
        }
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
