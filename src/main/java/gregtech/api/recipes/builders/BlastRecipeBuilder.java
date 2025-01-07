package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class BlastRecipeBuilder extends RecipeBuilder<BlastRecipeBuilder> {

    public BlastRecipeBuilder() {}

    public BlastRecipeBuilder(Recipe recipe, RecipeMap<BlastRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public BlastRecipeBuilder(BlastRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public BlastRecipeBuilder copy() {
        return new BlastRecipeBuilder(this);
    }

    @Override
    public boolean applyPropertyCT(@NotNull String key, @NotNull Object value) {
        if (key.equals(TemperatureProperty.KEY)) {
            this.blastFurnaceTemp(((Number) value).intValue());
            return true;
        }
        return super.applyPropertyCT(key, value);
    }

    public BlastRecipeBuilder blastFurnaceTemp(int blastFurnaceTemp) {
        if (blastFurnaceTemp <= 0) {
            GTLog.logger.error("Blast Furnace Temperature cannot be less than or equal to 0",
                    new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(TemperatureProperty.getInstance(), blastFurnaceTemp);
        return this;
    }

    public int getBlastFurnaceTemp() {
        return this.recipePropertyStorage.get(TemperatureProperty.getInstance(), 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(TemperatureProperty.getInstance().getKey(), getBlastFurnaceTemp())
                .toString();
    }
}
