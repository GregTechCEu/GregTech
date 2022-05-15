package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class GTRecipeFluidInput implements IGTRecipeInput {

    public int amount;
    boolean isConsumable = true;
    NBTMatcher nbtMatcher;
    NBTcondition nbtCondition;
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
        if (GTIngredientCache.FLUID_INSTANCES.get(realIngredient) == null) {
            GTIngredientCache.FLUID_INSTANCES.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = GTIngredientCache.FLUID_INSTANCES.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static GTRecipeFluidInput getOrCreate(IGTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeFluidInput(ri.getInputFluidStack(), i));
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
}
