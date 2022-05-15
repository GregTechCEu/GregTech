package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class GTRecipeItemInput implements IGTRecipeInput {
    int amount;
    boolean isConsumable = true;
    NBTMatcher nbtMatcher;
    NBTcondition nbtCondition;
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
        stack.setCount(amount);
        return getFromCache(new GTRecipeItemInput(stack));
    }

    private static GTRecipeItemInput getFromCache(GTRecipeItemInput realIngredient) {
        if (GTIngredientCache.INSTANCES.get(realIngredient) == null) {
            GTIngredientCache.INSTANCES.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = GTIngredientCache.INSTANCES.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static IGTRecipeInput getOrCreate(IGTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStack(), i));
    }

    public static IGTRecipeInput getOrCreate(IGTRecipeInput ri) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStack()));
    }

    public static IGTRecipeInput getOrCreate(ItemStack ri) {
        return getFromCache(new GTRecipeItemInput(ri));
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public GTRecipeItemInput setNonConsumable() {
        GTRecipeItemInput ret = new GTRecipeItemInput(this.inputStacks, this.amount);
        ret.isConsumable = false;
        if (GTIngredientCache.NON_CONSUMABLE_INSTANCES.get(ret) == null) {
            GTIngredientCache.NON_CONSUMABLE_INSTANCES.put(ret, new WeakReference<>(ret));
        } else {
            ret = GTIngredientCache.NON_CONSUMABLE_INSTANCES.get(ret).get();
        }
        return ret;
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
            return Objects.hash(stack.getItem(), stack.getMetadata(), stack.getTagCompound());
        }
        return Objects.hash(stack.getItem(), stack.getMetadata(), 0);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput otherIngredient = (GTRecipeItemInput) other;

        if (this.amount != otherIngredient.amount) return false;
        if (this.isConsumable != otherIngredient.isConsumable) return false;


        if (!this.getInputStack().isItemEqual(otherIngredient.getInputStack())) return false;

        if (nbtMatcher == null) {
                NBTTagCompound taga = null;
                NBTTagCompound tagb = null;
                if (this.getInputStack().hasTagCompound()) taga = this.getInputStack().getTagCompound();
                if (this.getInputStack().hasTagCompound())
                    tagb = otherIngredient.getInputStack().getTagCompound();
                if (taga == null && tagb != null) return false;
                else if (taga != null && !taga.equals(tagb)) return false;
        } else {
            return otherIngredient.nbtMatcher.evaluate(this.getInputStack(), otherIngredient.nbtCondition);
            }
        return true;
    }
}
