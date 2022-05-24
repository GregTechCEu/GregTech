package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class GTRecipeItemInput extends GTRecipeInput {
    ItemStack[] inputStacks;

    protected GTRecipeItemInput(ItemStack stack, int amount) {
        this.amount = amount;
        NonNullList<ItemStack> lst = NonNullList.create();
        if (stack.getMetadata() == GTValues.W ) {
            stack.getItem().getSubItems(net.minecraft.creativetab.CreativeTabs.SEARCH, lst);
        } else{
            lst.add(stack);
        }
        this.inputStacks = lst.stream().peek(is -> is.setCount(this.amount)).toArray(ItemStack[]::new);
    }

    protected GTRecipeItemInput(ItemStack[] stack, int amount) {
        this.amount = amount;
        this.inputStacks = Arrays.stream(stack).map(is -> {
            is = is.copy();
            is.setCount(this.amount);
            return is;
        }).toArray(ItemStack[]::new);
    }

    protected GTRecipeItemInput(ItemStack... stack) {
        this(stack, stack[0].getCount());
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
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks(), i));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks()));
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
    public ItemStack[] getInputStacks() {
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
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (this.inputStacks[0].getItem() == input.getItem()) {
            int meta = inputStacks[0].getMetadata();
            if (meta == GTValues.W || meta == input.getMetadata()) {
                return (nbtMatcher == null ? ItemStack.areItemStackTagsEqual(this.inputStacks[0], input) : nbtMatcher.evaluate(input, nbtCondition));
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (nbtMatcher == null) {
            return Objects.hash(Arrays.stream(inputStacks).map(ItemStack::getItem),
                    Arrays.stream(inputStacks).map(ItemStack::getMetadata),
                    this.amount, this.nbtMatcher, this.nbtCondition, isConsumable,
                    Arrays.stream(inputStacks).map(ItemStack::getTagCompound));
        }
        return Objects.hash(Arrays.stream(inputStacks).map(ItemStack::getItem),
                Arrays.stream(inputStacks).map(ItemStack::getMetadata),
                this.amount, this.nbtMatcher, this.nbtCondition, isConsumable,
                0);
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

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemStacksEqual(this.inputStacks[i], other.inputStacks[i])) return false;
        }
        return true;
    }
}
