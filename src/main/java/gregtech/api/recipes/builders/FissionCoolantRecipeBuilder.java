package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.FissionCoolantProperty;
import gregtech.api.recipes.properties.impl.PrimitiveProperty;
import gregtech.api.util.ValidationResult;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class FissionCoolantRecipeBuilder extends RecipeBuilder<FissionCoolantRecipeBuilder> {

    private boolean cutoffSet = false;

    public FissionCoolantRecipeBuilder() {}

    public FissionCoolantRecipeBuilder(Recipe recipe, RecipeMap<FissionCoolantRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FissionCoolantRecipeBuilder(FissionCoolantRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
        this.cutoffSet = recipeBuilder.cutoffSet;
    }

    @Override
    public FissionCoolantRecipeBuilder copy() {
        return new FissionCoolantRecipeBuilder(this);
    }

    private FissionCoolantProperty.FissionCoolantValues getProperty() {
        FissionCoolantProperty.FissionCoolantValues property;
        if (!this.recipePropertyStorage.contains(FissionCoolantProperty.getInstance())) {
            property = new FissionCoolantProperty.FissionCoolantValues();
            this.applyProperty(FissionCoolantProperty.getInstance(), property);
            return property;
        }
        property = this.recipePropertyStorage.get(FissionCoolantProperty.getInstance(), null);
        if (property == null) property = new FissionCoolantProperty.FissionCoolantValues();
        return property;
    }

    public FissionCoolantRecipeBuilder heatAbsorption(long euEquivalent) {
        getProperty().setHeatEquivalentPerOperation(euEquivalent);
        return this;
    }

    public FissionCoolantRecipeBuilder minimumTemperature(int temperature) {
        getProperty().setMinimumTemperature(temperature);
        return this;
    }

    public FissionCoolantRecipeBuilder cutoffTemperature(int temperature) {
        getProperty().setCutoffTemperature(temperature);
        cutoffSet = true;
        return this;
    }

    public long getHeatAbsorption() {
        return getProperty().getHeatEquivalentPerOperation();
    }

    public int getMinimumTemperature() {
        return getProperty().getMinimumTemperature();
    }

    public int getCutoffTemperature() {
        return getProperty().getCutoffTemperature();
    }

    @Override
    public ValidationResult<Recipe> build() {
        this.duration(1).EUt(1);
        applyProperty(PrimitiveProperty.getInstance(), true);
        if (!cutoffSet) {
            FissionCoolantProperty.FissionCoolantValues values = getProperty();
            values.setCutoffTemperature(values.getMinimumTemperature() + 500);
        }
        return super.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(FissionCoolantProperty.getInstance().getKey(), getProperty())
                .toString();
    }
}
