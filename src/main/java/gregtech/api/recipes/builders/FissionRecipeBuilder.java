package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.FissionProperty;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class FissionRecipeBuilder extends RecipeBuilder<FissionRecipeBuilder> {

    public FissionRecipeBuilder() {}

    public FissionRecipeBuilder(Recipe recipe, RecipeMap<FissionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FissionRecipeBuilder(FissionRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FissionRecipeBuilder copy() {
        return new FissionRecipeBuilder(this);
    }

    private FissionProperty.FissionValues getProperty() {
        FissionProperty.FissionValues property;
        if (!this.recipePropertyStorage.contains(FissionProperty.getInstance())) {
            property = new FissionProperty.FissionValues();
            this.applyProperty(FissionProperty.getInstance(), property);
            return property;
        }
        property = this.recipePropertyStorage.get(FissionProperty.getInstance(), null);
        if (property == null) property = new FissionProperty.FissionValues();
        return property;
    }

    public FissionRecipeBuilder heatOutput(long euEquivalent) {
        getProperty().setHeatEquivalentPerTick(euEquivalent);
        return this;
    }

    public FissionRecipeBuilder optimalTemperature(int temperature) {
        getProperty().setOptimalTemperature(temperature);
        return this;
    }

    public FissionRecipeBuilder heatPenalty(double multiplierPerKelvin) {
        getProperty().setSpeedMultiplierPerKelvin(multiplierPerKelvin);
        return this;
    }

    public long getHeatOutput() {
        return getProperty().getHeatEquivalentPerTick();
    }

    public int getOptimalTemperature() {
        return getProperty().getOptimalTemperature();
    }

    public double getHeatPenalty() {
        return getProperty().getSpeedMultiplierPerKelvin();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(FissionProperty.getInstance().getKey(), getProperty())
                .toString();
    }
}
