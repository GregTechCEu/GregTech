package gregtech.api.recipes.ingredients;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Objects;

public class GTRecipeFluidInput extends GTRecipeInput {
    FluidStack inputStack;

    public GTRecipeFluidInput(FluidStack inputStack) {
        this.inputStack = inputStack;
        this.amount = inputStack.amount;
    }

    public GTRecipeFluidInput(FluidStack inputStack, int amount) {
        this.inputStack = inputStack.copy();
        this.inputStack.amount = amount;
        this.amount = amount;
    }

    public static GTRecipeInput getOrCreate(FluidStack fluidStack, int amount) {
        return getFromCache(new GTRecipeFluidInput(fluidStack, amount));
    }

    public static GTRecipeInput getOrCreate(Fluid fluid, int amount) {
        return getFromCache(new GTRecipeFluidInput(new FluidStack(fluid, amount)));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeFluidInput(ri.getInputFluidStack(), i));
    }

    @Override
    protected GTRecipeFluidInput copy() {
        GTRecipeFluidInput copy = new GTRecipeFluidInput(this.inputStack, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        GTRecipeFluidInput copy = new GTRecipeFluidInput(this.inputStack, amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public FluidStack getInputFluidStack() {
        return inputStack;
    }

    @Override
    public boolean acceptsFluid(@Nullable FluidStack input) {
        if (input == null || input.amount == 0) {
            return false;
        }
        //the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but will still have the same fluidName.
        if (inputStack.getFluid().getName().equals(input.getFluid().getName())) {
            return (nbtMatcher == null ? FluidStack.areFluidStackTagsEqual(inputStack, input) : nbtMatcher.evaluate(input, nbtCondition));
        }
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
        if (inputStack.getFluid().getName().equals(other.inputStack.getFluid().getName())) {
            return FluidStack.areFluidStackTagsEqual(this.inputStack, other.inputStack);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (nbtMatcher == null) {
            //the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but will still have the same fluidName.
            return Objects.hash(inputStack.getFluid().getName(), this.amount, this.nbtMatcher, this.nbtCondition, inputStack.tag);
        }
        return Objects.hash(inputStack.getFluid().getName(), this.amount, this.nbtMatcher, this.nbtCondition, 0);
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeFluidInput)) {
            return false;
        }
        GTRecipeFluidInput other = (GTRecipeFluidInput) input;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        //the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but will still have the same fluidName.
        if (inputStack.getFluid().getName().equals(other.inputStack.getFluid().getName())) {
            return FluidStack.areFluidStackTagsEqual(this.inputStack, other.inputStack);
        }
        return false;
    }
}
