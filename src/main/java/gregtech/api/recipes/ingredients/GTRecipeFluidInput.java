package gregtech.api.recipes.ingredients;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GTRecipeFluidInput extends GTRecipeInput {

    private final FluidStack inputStack;

    public GTRecipeFluidInput(Fluid fluid, int amount) {
        this(new FluidStack(fluid, amount), amount);
    }

    public GTRecipeFluidInput(FluidStack inputStack) {
        this.inputStack = inputStack;
        this.amount = inputStack.amount;
    }

    public GTRecipeFluidInput(FluidStack inputStack, int amount) {
        this.inputStack = inputStack.copy();
        this.inputStack.amount = amount;
        this.amount = amount;
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(FluidStack fluidStack, int amount) {
        return new GTRecipeFluidInput(fluidStack, amount);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(Fluid fluid, int amount) {
        return new GTRecipeFluidInput(fluid, amount);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return new GTRecipeFluidInput(ri.getInputFluidStack(), i);
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
        if (input == null || input.amount == 0) return false;
        if (!areFluidsEqual(this.inputStack, input)) return false;
        if (this.nbtMatcher == null) {
            return FluidStack.areFluidStackTagsEqual(this.inputStack, input);
        } else {
            return this.nbtMatcher.evaluate(input, this.nbtCondition);
        }
    }

    @Override
    protected int computeHash() {
        return Objects.hash(this.inputStack.getFluid().getName(), this.amount, this.nbtMatcher, this.nbtCondition,
                this.nbtMatcher == null ? this.inputStack.tag : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeFluidInput)) {
            return false;
        }
        GTRecipeFluidInput other = (GTRecipeFluidInput) obj;

        if (this.amount != other.amount || this.isConsumable != other.isConsumable) return false;
        if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) return false;
        if (!Objects.equals(this.nbtCondition, other.nbtCondition)) return false;

        return areFluidsEqual(this.inputStack, other.inputStack) &&
                (this.nbtMatcher != null || FluidStack.areFluidStackTagsEqual(this.inputStack, other.inputStack));
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeFluidInput)) {
            return false;
        }
        GTRecipeFluidInput other = (GTRecipeFluidInput) input;

        if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) return false;
        if (!Objects.equals(this.nbtCondition, other.nbtCondition)) return false;

        return areFluidsEqual(this.inputStack, other.inputStack) &&
                (this.nbtMatcher != null || FluidStack.areFluidStackTagsEqual(this.inputStack, other.inputStack));
    }

    @Override
    public String toString() {
        return amount + "x" + inputStack.getUnlocalizedName();
    }

    // the Fluid registered to the fluidName on game load might not be the same Fluid after
    // loading the world, but will still have the same fluidName.
    private static boolean areFluidsEqual(FluidStack fluid1, FluidStack fluid2) {
        return fluid1.getFluid().getName().equals(fluid2.getFluid().getName());
    }
}
