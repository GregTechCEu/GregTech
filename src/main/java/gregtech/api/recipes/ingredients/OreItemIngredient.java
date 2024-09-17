package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Set;

public final class OreItemIngredient implements GTItemIngredient {

    private static short STANDARD = 0;

    private final OreItemIngredientBacker backer;
    private final NBTMatcher matcher;
    private final long count;

    OreItemIngredient(OreItemIngredientBacker backer, @Nullable NBTMatcher matcher,
                      @Range(from = 1, to = Long.MAX_VALUE) long count) {
        this.backer = backer;
        this.matcher = matcher;
        this.count = count;
    }

    public int getOreID() {
        return backer.getOreID();
    }

    @Override
    public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(@NotNull ItemStackMatchingContext context) {
        return backer.getMatchingStacksWithinContext(context);
    }

    @Nullable
    @Override
    public NBTMatcher getMatcher() {
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
        OreItemIngredient that = (OreItemIngredient) o;
        return Objects.equals(backer, that.backer) && Objects.equals(matcher, that.matcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backer, matcher);
    }

    public static class OreItemIngredientBacker extends StandardItemIngredient.ItemIngredientBacker {

        private short localStandard;
        private final int oreID;

        protected OreItemIngredientBacker(String ore) {
            this(OreDictionary.getOreID(ore));
        }

        public int getOreID() {
            return oreID;
        }

        protected OreItemIngredientBacker(int oreID) {
            super(new EnumMap<>(ItemStackMatchingContext.class));
            this.oreID = oreID;
        }

        @Override
        public @NotNull Collection<ItemStack> getMatchingStacksWithinContext(
                                                                             @NotNull ItemStackMatchingContext context) {
            if (localStandard != STANDARD) rebuildCache();
            return super.getMatchingStacksWithinContext(context);
        }

        @Override
        public boolean matches(ItemStack stack) {
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids) {
                if (id == oreID) return true;
            }
            return false;
        }

        protected void rebuildCache() {
            localStandard = STANDARD;
            matching.clear();
            Set<ItemStack> item = new ObjectOpenCustomHashSet<>(ItemStackApplicatorMap.ITEM);
            Set<ItemStack> itemDamage = new ObjectOpenCustomHashSet<>(ItemStackApplicatorMap.ITEM_DAMAGE);
            for (ItemStack stack : OreDictionary.getOres(OreDictionary.getOreName(oreID))) {
                if (stack.getItemDamage() == GTValues.W) item.add(stack);
                else itemDamage.add(stack);
            }
            if (!item.isEmpty())
                matching.put(ItemStackMatchingContext.ITEM, item);
            if (!itemDamage.isEmpty()) {
                // prevent matching on damage context if we've already matched on generic context
                itemDamage.removeIf(item::contains);
                matching.put(ItemStackMatchingContext.ITEM_DAMAGE, itemDamage);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OreItemIngredientBacker that = (OreItemIngredientBacker) o;
            return oreID == that.oreID;
        }

        @Override
        public int hashCode() {
            return 97 * oreID;
        }
    }

    public static void invalidateCaches() {
        STANDARD++;
    }
}
