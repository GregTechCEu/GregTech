package gregtech.api.recipes.builders;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.ImplosionExplosiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.ToStringBuilder;
import stanhebben.zenscript.annotations.ZenMethod;

public class ImplosionRecipeBuilder extends RecipeBuilder<ImplosionRecipeBuilder> {

    protected int explosivesAmount;
    protected ItemStack explosivesType;

    public ImplosionRecipeBuilder() {
    }

    public ImplosionRecipeBuilder(Recipe recipe, RecipeMap<ImplosionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
        this.explosivesType = recipe.getProperty(ImplosionExplosiveProperty.getInstance(), ItemStack.EMPTY);
    }

    public ImplosionRecipeBuilder(RecipeBuilder<ImplosionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public ImplosionRecipeBuilder copy() {
        return new ImplosionRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(String key, Object value) {
        super.applyProperty(key, value);
        if (key.equals(ImplosionExplosiveProperty.KEY)) {
            explosivesAmount((int) value);
            return true;
        }
        return false;
    }

    @Override
    public boolean applyProperty(String key, ItemStack exploType) {
        super.applyProperty(key, exploType);
        if (key.equals("explosives")) {
            explosivesAmount(exploType.getCount());
            explosivesType = exploType;
            return true;
        }
        return false;
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosivesAmount(int explosivesAmount) {
        if (!GTUtility.isBetweenInclusive(1, 64, explosivesAmount)) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.explosivesAmount = explosivesAmount;
        return this;
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosivesType(ItemStack explosivesType) {
        this.explosivesType = explosivesType;
        if (!GTUtility.isBetweenInclusive(1, 64, explosivesType.getCount())) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.explosivesAmount = explosivesType.getCount();
        return this;
    }

    public ItemStack getExplosivesType() {
        return this.explosivesType;
    }

    public ValidationResult<Recipe> build() {

        //Adjust the explosive type and the explosive amount. This is done here because it was null otherwise, for some reason
        if (explosivesType == null) {
            this.explosivesType = new ItemStack(Blocks.TNT, explosivesAmount);
        } else {
            this.explosivesType = new ItemStack(explosivesType.getItem(), explosivesAmount, explosivesType.getMetadata());
        }
        inputs.add(CountableIngredient.from(explosivesType));


        Recipe recipe = new Recipe(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs,
                duration, EUt, hidden, isCTRecipe);

        if (!recipe.setProperty(ImplosionExplosiveProperty.getInstance(), explosivesType)) {
            return ValidationResult.newResult(EnumValidationResult.INVALID, recipe);
        }

        return ValidationResult.newResult(finalizeAndValidate(), recipe);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ImplosionExplosiveProperty.getInstance().getKey(), explosivesType)
                .toString();
    }
}
