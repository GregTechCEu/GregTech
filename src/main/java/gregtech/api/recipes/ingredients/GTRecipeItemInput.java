package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.WeakHashMap;

public class GTRecipeItemInput extends GTRecipeInput {
    ItemStack inputStack;

    protected GTRecipeItemInput(ItemStack stack) {
        this.inputStack = stack;
        amount = stack.getCount();
    }

    protected GTRecipeItemInput(ItemStack stack, int amount) {
        this.inputStack = stack;
        this.inputStack.setCount(amount);
        this.amount = amount;
    }

    public static GTRecipeItemInput getOrCreate(ItemStack stack, int amount) {
        return getFromCache(new GTRecipeItemInput(stack, amount));
    }

    private static GTRecipeItemInput getFromCache(GTRecipeItemInput realIngredient) {
        WeakHashMap<GTRecipeItemInput, WeakReference<GTRecipeItemInput>> cache;
        if (realIngredient.isNonConsumable()) {
            cache = GTIngredientCache.NON_CONSUMABLE_INSTANCES;
        } else {
            cache = GTIngredientCache.INSTANCES;
        }
        if (cache.get(realIngredient) == null) {
            cache.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = cache.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStack(), i));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStack()));
    }

    public static GTRecipeInput getOrCreate(ItemStack ri) {
        return getFromCache(new GTRecipeItemInput(ri));
    }

    protected GTRecipeItemInput copy() {
        GTRecipeItemInput copy = new GTRecipeItemInput(this.inputStack, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    GTRecipeInput getFromCache(GTRecipeInput recipeInput) {
        return getFromCache((GTRecipeItemInput) recipeInput);
    }

    @Override
    public ItemStack getInputStack() {
        return this.inputStack;
    }

    @Override
    public FluidStack getInputFluidStack() {
        return null;
    }

    @Override
    public boolean isOreDict() {
        return false;
    }

    @Override
    public boolean acceptsStack(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (this.inputStack.getItem() == input.getItem()) {
            int meta = inputStack.getMetadata();
            if (meta == GTValues.W || meta == input.getMetadata()) {
                return (nbtMatcher == null ? ItemStack.areItemStackTagsEqual(this.inputStack, input) : nbtMatcher.evaluate(input, nbtCondition));
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (nbtMatcher == null) {
            return Objects.hash(inputStack.getItem(), inputStack.getMetadata(), this.amount, this.nbtMatcher, this.nbtCondition, isConsumable, inputStack.getTagCompound());
        }
        return Objects.hash(inputStack.getItem(), inputStack.getMetadata(), this.amount, this.nbtMatcher, this.nbtCondition, isConsumable, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput other = (GTRecipeItemInput) obj;

        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;

        return this.getInputStack().isItemEqual(other.getInputStack()) && ItemStack.areItemStackTagsEqual(this.getInputStack(), other.getInputStack());
    }
}
