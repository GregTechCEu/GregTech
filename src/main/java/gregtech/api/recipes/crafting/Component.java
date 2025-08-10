package gregtech.api.recipes.crafting;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Component {

    private final @NotNull Int2ObjectMap<ItemStack> itemEntries;
    private final @NotNull Int2ObjectMap<String> stringEntries;
    private @Nullable Object fallbackValue;

    private Component(@NotNull Int2ObjectMap<ItemStack> itemEntries,
                      @NotNull Int2ObjectMap<String> stringEntries,
                      @Nullable Object fallbackValue) {
        this.itemEntries = itemEntries;
        this.stringEntries = stringEntries;
        this.fallbackValue = fallbackValue;
    }

    /**
     * @param tier the tier of the ingredient
     * @return the raw ingredient, which may be either an {@link ItemStack} or {@link String}
     */
    public @Nullable Object getIngredient(int tier) {
        ItemStack stack = itemEntries.get(tier);
        if (stack != null) {
            return stack;
        }
        String string = stringEntries.get(tier);
        if (string != null) {
            return string;
        }
        return fallbackValue;
    }

    /**
     * Does not update the fallback ingredient
     *
     * @see #updateIngredients(Component, boolean)
     */
    @SuppressWarnings("unused")
    public void updateIngredients(@NotNull Component other) {
        updateIngredients(other, false);
    }

    /**
     * Add all ingredients from the provided Crafting Component to this ingredient, replacing existing values with
     * new ones.
     *
     * @param other          the component to update with
     * @param updateFallback if the fallback ingredient should be updated with the provided component's ingredient
     */
    @SuppressWarnings("unused")
    public void updateIngredients(@NotNull Component other, boolean updateFallback) {
        itemEntries.putAll(other.itemEntries);
        stringEntries.putAll(other.stringEntries);
        if (updateFallback) {
            if (other.fallbackValue == null) {
                throw new IllegalArgumentException("Cannot update the fallback value to null");
            }
            this.fallbackValue = other.fallbackValue;
        }
    }

    public static class Builder {

        private final @NotNull Int2ObjectMap<ItemStack> itemEntries = new Int2ObjectOpenHashMap<>();
        private final @NotNull Int2ObjectMap<String> stringEntries = new Int2ObjectOpenHashMap<>();
        private @Nullable Object fallbackValue;

        /**
         * Create a CraftingComponent without a fallback value.
         */
        public Builder() {}

        /**
         * @param fallback the fallback ingredient
         */
        public Builder(@NotNull ItemStack fallback) {
            this.fallbackValue = fallback;
        }

        /**
         * @see #Builder(ItemStack)
         */
        public Builder(@NotNull MetaItem<?>.MetaValueItem fallback) {
            this(fallback.getStackForm());
        }

        /**
         * @see #Builder(ItemStack)
         */
        public Builder(@NotNull MetaTileEntity fallback) {
            this(fallback.getStackForm());
        }

        /**
         * @see #Builder(Block, int) but defaults to {@link GTValues#W} for meta
         */
        public Builder(@NotNull Block fallback) {
            this(fallback, GTValues.W);
        }

        /**
         * @see #Builder(ItemStack)
         */
        public Builder(@NotNull Block fallback, int meta) {
            this(new ItemStack(fallback, 1, meta));
        }

        /**
         * @param fallbackOreDict an OreDict string for the fallback ingredient
         */
        public Builder(@NotNull String fallbackOreDict) {
            this.fallbackValue = fallbackOreDict;
        }

        /**
         * @see #Builder(String)
         */
        public Builder(@NotNull OrePrefix fallbackPrefix, @NotNull Material fallbackMaterial) {
            this(new UnificationEntry(fallbackPrefix, fallbackMaterial));
        }

        /**
         * @see #Builder(String)
         */
        public Builder(@NotNull UnificationEntry fallback) {
            this(fallback.toString());
        }

        /**
         * Add an entry
         *
         * @param tier  the voltage tier for the entry, see {@link GTValues#V}
         * @param stack the ingredient for the entry
         * @return this
         */
        public @NotNull Builder entry(int tier, @NotNull ItemStack stack) {
            itemEntries.put(tier, stack);
            stringEntries.remove(tier);
            return this;
        }

        /**
         * @see #entry(int, ItemStack)
         */
        public @NotNull Builder entry(int tier, @NotNull MetaItem<?>.MetaValueItem metaValueItem) {
            return entry(tier, metaValueItem.getStackForm());
        }

        /**
         * @see #entry(int, ItemStack)
         */
        public @NotNull Builder entry(int tier, @NotNull MetaTileEntity metaTileEntity) {
            return entry(tier, metaTileEntity.getStackForm());
        }

        /**
         * @see #entry(int, Block, int) but defaults to {@link GTValues#W} for meta
         */
        public @NotNull Builder entry(int tier, @NotNull Block block) {
            return entry(tier, new ItemStack(block, 1, GTValues.W));
        }

        /**
         * @see #entry(int, ItemStack)
         */
        public @NotNull Builder entry(int tier, @NotNull Block block, int meta) {
            return entry(tier, new ItemStack(block, 1, meta));
        }

        /**
         * Add an entry
         *
         * @param tier    the voltage tier for the entry, see {@link GTValues#V}
         * @param oreDict the OreDict for the entry
         * @return this
         */
        public @NotNull Builder entry(int tier, @NotNull String oreDict) {
            stringEntries.put(tier, oreDict);
            itemEntries.remove(tier);
            return this;
        }

        /**
         * @see #entry(int, String)
         */
        public @NotNull Builder entry(int tier, @NotNull OrePrefix prefix, @NotNull Material material) {
            return entry(tier, new UnificationEntry(prefix, material));
        }

        /**
         * @see #entry(int, String)
         */
        public @NotNull Builder entry(int tier, @NotNull UnificationEntry unificationEntry) {
            return entry(tier, unificationEntry.toString());
        }

        public @NotNull Component build() {
            return new Component(itemEntries, stringEntries, fallbackValue);
        }
    }
}
