package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
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

public class GTRecipeOreInput implements IGTRecipeInput {
    int amount;
    boolean isConsumable = true;
    NBTMatcher nbtMatcher;
    NBTcondition nbtCondition;
    int ore;

    protected GTRecipeOreInput(String ore, int amount) {
        this.ore = OreDictionary.getOreID(ore);
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
        if (GTIngredientCache.ORE_INSTANCES.get(realIngredient) == null) {
            GTIngredientCache.ORE_INSTANCES.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = GTIngredientCache.ORE_INSTANCES.get(realIngredient).get();
        }
        return realIngredient;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public IGTRecipeInput setNonConsumable() {
        this.isConsumable = false;
        return this;
    }

    @Override
    public IGTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTcondition nbtCondition) {
        this.nbtMatcher = nbtMatcher;
        this.nbtCondition = nbtCondition;
        return this;
    }

    @Override
    public boolean hasNBTMatchingCondition() {
        return this.nbtMatcher != null;
    }

    @Override
    public NBTMatcher getNBTMatcher() {
        return nbtMatcher;
    }

    @Override
    public NBTcondition getNBTMatchingCondition() {
        return nbtCondition;
    }

    @Override
    public boolean isNonConsumable() {
        return !isConsumable;
    }

    @Override
    public ItemStack getInputStack() {
        return ItemStack.EMPTY;
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
        return Arrays.stream(OreDictionary.getOreIDs(input)).anyMatch(id -> id == ore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, ore);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GTRecipeOreInput)) {
            return false;
        }
        GTRecipeOreInput other = (GTRecipeOreInput) obj;
        return amount == other.amount && ore == other.ore;
    }
}
