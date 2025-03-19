package gregtech.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.properties.impl.ImplosionExplosiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import stanhebben.zenscript.annotations.ZenMethod;

public class ImplosionRecipeBuilder extends RecipeBuilder<ImplosionRecipeBuilder> {

    public ImplosionRecipeBuilder() {}

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
    public boolean applyPropertyCT(@NotNull String key, @NotNull Object value) {
        if (key.equals(ImplosionExplosiveProperty.KEY)) {
            if (value instanceof ItemStack stack) {
                return this.applyProperty(ImplosionExplosiveProperty.getInstance(), stack);
            } else if (value instanceof Number number) {
                return this.applyProperty(ImplosionExplosiveProperty.getInstance(), number.intValue());
            }
            return false;
        }
        return super.applyPropertyCT(key, value);
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosives(int amount) {
        return explosives(new ItemStack(Blocks.TNT, amount));
    }

    @ZenMethod
    public ImplosionRecipeBuilder explosives(@NotNull ItemStack explosive) {
        if (explosive.isEmpty()) {
            GTLog.logger.error("Cannot use empty explosives", new Throwable());
            this.recipeStatus = EnumValidationResult.INVALID;
            return this;
        }

        int count = explosive.getCount();
        if (count < 1 || count > 64) {
            GTLog.logger.error("Amount of explosives should be from 1 to 64 inclusive", new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return this;
        }
        if (this.applyProperty(ImplosionExplosiveProperty.getInstance(), explosive)) {
            this.inputs.add(new GTRecipeItemInput(explosive));
        }
        return this;
    }

    public @NotNull ItemStack getExplosives() {
        return this.recipePropertyStorage.get(ImplosionExplosiveProperty.getInstance(), ItemStack.EMPTY);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ImplosionExplosiveProperty.getInstance().getKey(), getExplosives())
                .toString();
    }
}
