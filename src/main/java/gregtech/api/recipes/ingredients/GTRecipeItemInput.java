package gregtech.api.recipes.ingredients;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.WeakHashMap;

public class GTRecipeItemInput extends GTRecipeInput {
    ItemStack inputStacks;

    protected GTRecipeItemInput(ItemStack stack) {
        this.inputStacks = stack;
        amount = stack.getCount();
    }

    protected GTRecipeItemInput(ItemStack stack, int amount) {
        this.inputStacks = stack;
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
        GTRecipeItemInput copy = new GTRecipeItemInput(this.inputStacks, this.amount);
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
        return this.inputStacks;
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
        if (input == null) {
            return false;
        }
        ItemStack stacks = this.inputStacks;
        if (stacks.getItem() == input.getItem()) {
            int i = stacks.getMetadata();
            if (i == 32767 || i == input.getMetadata()) {
                return (nbtMatcher == null ? ItemStack.areItemStackTagsEqual(stacks, input) : nbtMatcher.evaluate(stacks, nbtCondition));
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        ItemStack stack = getInputStack();
        if (nbtMatcher == null) {
            return Objects.hash(stack.getItem(), stack.getMetadata(), this.amount, this.nbtMatcher, this.nbtCondition, stack.getTagCompound());
        }
        return Objects.hash(stack.getItem(), stack.getMetadata(), this.amount, this.nbtMatcher, this.nbtCondition, 0);
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
