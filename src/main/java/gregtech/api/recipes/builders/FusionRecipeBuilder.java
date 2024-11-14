package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.properties.impl.FusionEUToStartProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class FusionRecipeBuilder extends RecipeBuilder<FusionRecipeBuilder> {

    public FusionRecipeBuilder() {}

    public FusionRecipeBuilder(Recipe recipe, RecipeMap<FusionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FusionRecipeBuilder(RecipeBuilder<FusionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FusionRecipeBuilder copy() {
        return new FusionRecipeBuilder(this);
    }

    public FusionRecipeBuilder EUToStart(long EUToStart) {
        if (EUToStart <= 0) {
            GTLog.logger.error("EU to start cannot be less than or equal to 0", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(FusionEUToStartProperty.getInstance(), EUToStart);
        return this;
    }

    public long getEUToStart() {
        return this.recipePropertyStorage.get(FusionEUToStartProperty.getInstance(), 0L);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(FusionEUToStartProperty.getInstance().getKey(), getEUToStart())
                .toString();
    }
}
