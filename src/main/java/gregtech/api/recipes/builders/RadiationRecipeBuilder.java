package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.RadiationProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RadiationRecipeBuilder extends RecipeBuilder<RadiationRecipeBuilder> {
    public RadiationRecipeBuilder() {
    }

    public RadiationRecipeBuilder(Recipe recipe, RecipeMap<RadiationRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public RadiationRecipeBuilder(RecipeBuilder<RadiationRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, @Nullable Object value) {
        if (key.equals(RadiationProperty.KEY)) {
            this.applyProperty(RadiationProperty.getInstance(), value);
        }
        return super.applyProperty(key, value);
    }

    public RadiationRecipeBuilder rads(float val) {
        this.applyProperty(RadiationProperty.KEY, val);
        return this;
    }

    public float getRadiationValue() {
        if (this.recipePropertyStorage == null) {
            return 0.0f;
        }
        return this.recipePropertyStorage.getRecipePropertyValue(RadiationProperty.getInstance(), 0.0f);
    }

    @Override
    public RadiationRecipeBuilder copy() {
        return new RadiationRecipeBuilder(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(RadiationProperty.getInstance().getKey(), getRadiationValue())
                .toString();
    }
}
