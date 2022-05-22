package gregtech.api.recipes.ingredients;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class GTRecipeFluidInput extends GTRecipeInput {
    FluidStack inputStacks;

    public GTRecipeFluidInput(FluidStack inputStacks) {
        this.inputStacks = inputStacks;
        this.amount = inputStacks.amount;
    }

    public GTRecipeFluidInput(FluidStack inputStacks, int amount) {
        this.inputStacks = inputStacks;
        this.amount = amount;
    }

    public static GTRecipeFluidInput getOrCreate(FluidStack fluidStack, int amount) {
        fluidStack.amount = amount;
        return getFromCache(new GTRecipeFluidInput(fluidStack));
    }

    public static GTRecipeFluidInput getOrCreate(Fluid fluid, int amount) {
        return getFromCache(new GTRecipeFluidInput(new FluidStack(fluid, amount)));
    }

    private static GTRecipeFluidInput getFromCache(GTRecipeFluidInput realIngredient) {
        WeakHashMap<GTRecipeFluidInput, WeakReference<GTRecipeFluidInput>> cache;
        if (realIngredient.isNonConsumable()) {
            cache = GTIngredientCache.NON_CONSUMABLE_FLUID_INSTANCES;
        } else {
            cache = GTIngredientCache.FLUID_INSTANCES;
        }
        if (cache.get(realIngredient) == null) {
            cache.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = cache.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static GTRecipeFluidInput getOrCreate(GTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeFluidInput(ri.getInputFluidStack(), i));
    }

    protected GTRecipeFluidInput copy() {
        GTRecipeFluidInput copy = new GTRecipeFluidInput(this.inputStacks, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    GTRecipeFluidInput getFromCache(GTRecipeInput recipeInput) {
        return GTRecipeFluidInput.getFromCache((GTRecipeFluidInput) recipeInput);
    }

    @Override
    public FluidStack getInputFluidStack() {
        return inputStacks;
    }

    @Override
    public boolean isOreDict() {
        return false;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack input) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeFluidInput)) {
            return false;
        }
        GTRecipeFluidInput other = (GTRecipeFluidInput) obj;
        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return this.inputStacks.isFluidEqual(other.inputStacks);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
