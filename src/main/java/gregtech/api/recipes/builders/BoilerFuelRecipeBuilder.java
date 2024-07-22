package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.BoilerFuelProperty;
import gregtech.api.recipes.recipeproperties.EmptyRecipePropertyStorage;
import gregtech.api.recipes.recipeproperties.RecipePropertyStorage;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;

import it.unimi.dsi.fastutil.ints.IntLists;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class BoilerFuelRecipeBuilder extends RecipeBuilder<BoilerFuelRecipeBuilder> {
    public static final int FLUID_DRAIN_MULTIPLIER = 100;
    public BoilerFuelRecipeBuilder() {}

    public BoilerFuelRecipeBuilder(Recipe recipe, RecipeMap<BoilerFuelRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public BoilerFuelRecipeBuilder(RecipeBuilder<BoilerFuelRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public BoilerFuelRecipeBuilder copy() {
        return new BoilerFuelRecipeBuilder(this);
    }

    @Override
    public BoilerFuelRecipeBuilder fluidInputs(Collection<GTRecipeInput> fluidIngredients) {
        for(GTRecipeInput fluidInput : fluidIngredients) {
            this.fluidInputs(fluidInput);
        }
        return this;
    }

    @Override
    public BoilerFuelRecipeBuilder fluidInputs(GTRecipeInput fluidIngredient) {
        this.fluidInputs.add(fluidIngredient.copyWithAmount(fluidIngredient.getAmount() * FLUID_DRAIN_MULTIPLIER));
        return this;
    }

    @Override
    public BoilerFuelRecipeBuilder fluidInputs(FluidStack input) {
        if (input != null && input.amount > 0) {
            input.amount *= FLUID_DRAIN_MULTIPLIER;
            this.fluidInputs.add(new GTRecipeFluidInput(input));
        } else if (input != null) {
            GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", input.amount, new Throwable());
        } else {
            GTLog.logger.error("FluidStack cannot be null.");
        }
        return this;
    }

    @Override
    public BoilerFuelRecipeBuilder fluidInputs(FluidStack... fluidStacks) {
        for (FluidStack fluidStack : fluidStacks) {
            this.fluidInputs(fluidStack);
        }
        return this;
    }

    @Override
    public BoilerFuelRecipeBuilder duration(int duration) {
        this.duration = duration * FLUID_DRAIN_MULTIPLIER;
        switch(recipePropertyStorage.getRecipePropertyValue(BoilerFuelProperty.getInstance(), "")) {
            case "diesel": this.duration /= 2; break;
            case "dense" : this.duration *= 2; break;
        }
        return this;
    }

    public BoilerFuelRecipeBuilder fuelType(String fuelType) {
        this.applyProperty(BoilerFuelProperty.getInstance(), fuelType);
        return this;
    }
}
