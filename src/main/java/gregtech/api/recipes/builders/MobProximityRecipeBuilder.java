package gregtech.api.recipes.builders;

import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.CauseDamageProperty;
import gregtech.api.recipes.recipeproperties.MobOnTopProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.ValidationResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;

import static gregtech.api.GTValues.MAX;
import static gregtech.api.GTValues.V;

public class MobProximityRecipeBuilder extends RecipeBuilder<MobProximityRecipeBuilder> {

    private ResourceLocation entityID;
    private float damage;

    public MobProximityRecipeBuilder() {
    }

    public MobProximityRecipeBuilder(Recipe recipe, RecipeMap<MobProximityRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
        this.entityID = recipe.getProperty(MobOnTopProperty.getInstance(), EntityList.LIGHTNING_BOLT);
    }

    public MobProximityRecipeBuilder(RecipeBuilder<MobProximityRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public MobProximityRecipeBuilder copy() {
        return new MobProximityRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(String key, Object value) {
        if (key.equals(MobOnTopProperty.KEY)) {
            this.mob((ResourceLocation) value);
            return true;
        }
        return true;
    }

    public MobProximityRecipeBuilder mob(ResourceLocation entityID) {
        this.entityID = entityID;
        return this;
    }

    public MobProximityRecipeBuilder mob(Class<? extends Entity> clazz) {
        this.entityID = EntityList.getKey(clazz);
        return this;
    }

    public MobProximityRecipeBuilder causeDamage(float damage) {
        this.damage = damage;
        return this;
    }

    public ValidationResult<Recipe> build() {
        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
                duration, EUt, hidden);
        for (ItemStack stack : this.getInputs().get(0).getIngredient().getMatchingStacks()){
            if (this.recipeMap.findRecipe(V[MAX], Collections.singletonList(stack),
                    Collections.EMPTY_LIST, Integer.MAX_VALUE, MatchingMode.IGNORE_FLUIDS) != null) {
                GTLog.logger.error("Input items of Mob Extractor recipes must not conflict", new IllegalArgumentException());
                return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
            }
        }

        if (!recipe.setProperty(MobOnTopProperty.getInstance(), entityID)) {
            return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
        }
        if (this.damage != 0) {
            if (!recipe.setProperty(CauseDamageProperty.getInstance(), damage)) {
                return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
            }
        }

        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(MobOnTopProperty.getInstance().getKey(), entityID.toString())
                .append(CauseDamageProperty.getInstance().getKey(), damage)
                .toString();
    }
}
