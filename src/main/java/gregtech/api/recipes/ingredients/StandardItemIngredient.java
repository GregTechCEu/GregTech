package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Set;

public final class StandardItemIngredient implements GTItemIngredient {

    private final ItemIngredientBacker backer;
    private final @Nullable NBTMatcher matcher;
    private final long count;

    StandardItemIngredient(@NotNull StandardItemIngredient.ItemIngredientBacker backer, @Nullable NBTMatcher matcher,
                           @Range(from = 1, to = Long.MAX_VALUE) long count) {
        this.backer = backer;
        this.matcher = matcher;
        this.count = count;
    }

    @Override
    public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(@NotNull ItemStackMatchingContext context) {
        return backer.getMatchingStacksWithinContext(context);
    }

    @Override
    public @Nullable NBTMatcher getMatcher() {
        return matcher;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return (matcher == null || matcher.matches(stack)) && backer.matches(stack);
    }

    @Override
    public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StandardItemIngredient that = (StandardItemIngredient) o;
        return Objects.equals(backer, that.backer) && Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backer, matcher);
    }

    public static class ItemIngredientBacker {

        protected final @NotNull EnumMap<ItemStackMatchingContext, Collection<ItemStack>> matching;

        protected ItemIngredientBacker(@NotNull EnumMap<ItemStackMatchingContext, Collection<ItemStack>> matching) {
            this.matching = matching;
        }

        public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(
                                                                             @NotNull ItemStackMatchingContext context) {
            return matching.getOrDefault(context, Collections.emptyList());
        }

        public boolean matches(ItemStack stack) {
            for (var matchInformation : matching.entrySet()) {
                ItemStackMatchingContext context = matchInformation.getKey();
                for (ItemStack match : matchInformation.getValue()) {
                    if (match.getItem() == stack.getItem() &&
                            (!context.matchesDamage() || match.getItemDamage() == stack.getItemDamage()) &&
                            (!context.matchesNBT() || Objects.equals(match.getTagCompound(), stack.getTagCompound()))) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemIngredientBacker that = (ItemIngredientBacker) o;
            return Objects.equals(matching, that.matching);
        }

        @Override
        public int hashCode() {
            return 97 * matching.hashCode();
        }
    }

    @Contract(" -> new")
    public static @NotNull ItemIngredientBuilder builder() {
        return new ItemIngredientBuilder();
    }

    public static class ItemIngredientBuilder {

        private final EnumMap<ItemStackMatchingContext, Collection<ItemStack>> matching = new EnumMap<>(
                ItemStackMatchingContext.class);

        private Set<ItemStack> stacks = new ObjectOpenCustomHashSet<>(ItemStackApplicatorMap.ITEM_DAMAGE_NBT);
        private @Nullable NBTMatcher matcher = null;

        private long count = 1;

        public ItemIngredientBuilder setCount(long count) {
            this.count = count;
            return this;
        }

        public ItemIngredientBuilder setMatcher(NBTMatcher matcher) {
            this.matcher = matcher;
            return this;
        }

        public ItemIngredientBuilder addStack(ItemStack item) {
            stacks.add(item);
            return this;
        }

        public ItemIngredientBuilder addItem(Item item) {
            stacks.add(new ItemStack(item));
            return this;
        }

        public ItemIngredientBuilder addBlock(Block block) {
            stacks.add(new ItemStack(block));
            return this;
        }

        public ItemIngredientBuilder addBlock(Block block, int damage) {
            stacks.add(new ItemStack(block, damage));
            return this;
        }

        public ItemIngredientBuilder addBlock(Block block, int damage, NBTTagCompound tag) {
            ItemStack stack = new ItemStack(block, 1, damage);
            stack.setTagCompound(tag);
            stacks.add(stack);
            return this;
        }

        public ItemIngredientBuilder addItem(Item item, int damage) {
            stacks.add(new ItemStack(item, 1, damage));
            return this;
        }

        public ItemIngredientBuilder addItem(Item item, int damage, NBTTagCompound tag) {
            ItemStack stack = new ItemStack(item, 1, damage);
            stack.setTagCompound(tag);
            stacks.add(stack);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public ItemIngredientBuilder clearToContext(@NotNull ItemStackMatchingContext context) {
            matching.put(context, stacks);
            stacks = new ObjectOpenHashSet<>();
            matcher = null;
            return this;
        }

        public StandardItemIngredient clearToContextAndBuild(@NotNull ItemStackMatchingContext context) {
            clearToContext(context);
            return IngredientCache.getItemIngredient(matching, count, matcher);
        }
    }
}
