package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.recipeproperties.ImplosionExplosiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ValidationResult;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.builder.ToStringBuilder;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;

public class ImplosionRecipeBuilder extends RecipeBuilder<ImplosionRecipeBuilder> {

    public ImplosionRecipeBuilder() {

    }

    public ImplosionRecipeBuilder(Recipe recipe, RecipeMap<ImplosionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public ImplosionRecipeBuilder(RecipeBuilder<ImplosionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public ImplosionRecipeBuilder copy() {
        return new ImplosionRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, Object value) {
        if (key.equals(ImplosionExplosiveProperty.KEY)) {
            if (value instanceof ItemStack) {
                this.applyProperty(ImplosionExplosiveProperty.getInstance(), value);
            } else {
                this.applyProperty(ImplosionExplosiveProperty.getInstance(), new ItemStack(Blocks.TNT, (int) value));
            }
            return true;
        }
        return super.applyProperty(key, value);
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosivesAmount(int explosivesAmount) {
        if (!GTUtility.isBetweenInclusive(1, 64, explosivesAmount)) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(ImplosionExplosiveProperty.getInstance(), new ItemStack(Blocks.TNT, explosivesAmount));
        return this;
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosivesType(ItemStack explosivesType) {
        if (!GTUtility.isBetweenInclusive(1, 64, explosivesType.getCount())) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(ImplosionExplosiveProperty.getInstance(), explosivesType);
        return this;
    }

    public ItemStack getExplosivesType() {
        if (this.recipePropertyStorage == null) {
            return ItemStack.EMPTY;
        }
        return this.recipePropertyStorage.getRecipePropertyValue(ImplosionExplosiveProperty.getInstance(), ItemStack.EMPTY);
    }

    public ValidationResult<Recipe> build() {
        ItemStack explosivesType = getExplosivesType();
        if (!explosivesType.isEmpty()) {
            this.inputs.add(GTRecipeItemInput.getOrCreate(explosivesType));
        } else {
            this.recipePropertyStorageErrored = true;
        }
        return super.build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ImplosionExplosiveProperty.getInstance().getKey(), getExplosivesType())
                .toString();
    }
}
