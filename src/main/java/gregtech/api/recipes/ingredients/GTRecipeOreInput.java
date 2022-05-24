package gregtech.api.recipes.ingredients;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class GTRecipeOreInput extends GTRecipeInput {
    int ore;
    ItemStack[] inputStacks;

    protected GTRecipeOreInput(String ore, int amount) {
        this.ore = OreDictionary.getOreID(ore);
        this.amount = amount;
    }

    protected GTRecipeOreInput(int ore, int amount) {
        this.ore = ore;
        this.amount = amount;
    }

    public static GTRecipeOreInput getOrCreate(String ore, int amount) {
        return getFromCache(new GTRecipeOreInput(ore, amount));
    }

    public static GTRecipeOreInput getOrCreate(String ore) {
        return getFromCache(new GTRecipeOreInput(ore, 1));
    }

    public static GTRecipeOreInput getOrCreate(OrePrefix prefix, Material material, int amount) {
        return getOrCreate(new UnificationEntry(prefix, material).toString(), amount);
    }

    public static GTRecipeOreInput getOrCreate(OrePrefix prefix, Material material) {
        return getOrCreate(new UnificationEntry(prefix, material).toString(), 1);
    }

    private static GTRecipeOreInput getFromCache(GTRecipeOreInput realIngredient) {
        WeakHashMap<GTRecipeOreInput, WeakReference<GTRecipeOreInput>> cache;
        if (realIngredient.isNonConsumable()) {
            cache = GTIngredientCache.NON_CONSUMABLE_ORE_INSTANCES;
        } else {
            cache = GTIngredientCache.ORE_INSTANCES;
        }
        if (cache.get(realIngredient) == null) {
            cache.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = cache.get(realIngredient).get();
        }
        return realIngredient;
    }

    protected GTRecipeOreInput copy() {
        GTRecipeOreInput copy = new GTRecipeOreInput(ore, amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    GTRecipeInput getFromCache(GTRecipeInput recipeInput) {
        return getFromCache((GTRecipeOreInput) recipeInput);
    }

    @Override
    public ItemStack[] getInputStacks() {
        if (this.inputStacks == null) {
            inputStacks = (OreDictionary.getOres(OreDictionary.getOreName(ore)).stream().map(is -> {
                is = is.copy();
                is.setCount(this.amount);
                return is;
            })).toArray(ItemStack[]::new);
        }
        return inputStacks;
    }

    @Override
    public FluidStack getInputFluidStack() {
        return null;
    }

    @Override
    public boolean isOreDict() {
        return true;
    }

    @Override
    public int getOreDict() {
        return ore;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (Arrays.stream(OreDictionary.getOreIDs(input)).noneMatch(id -> id == ore)) {
            return false;
        }
        if (nbtMatcher != null) {
            return nbtMatcher.evaluate(input, nbtCondition);
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, ore, isConsumable, nbtMatcher, nbtCondition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeOreInput)) {
            return false;
        }
        GTRecipeOreInput other = (GTRecipeOreInput) obj;
        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return ore == other.ore;
    }
}
