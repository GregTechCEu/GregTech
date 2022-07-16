package gregtech.api.recipes.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.ingredients.*;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.RecipeBuilder")
@ZenRegister
public class CTRecipeBuilder {

    public final RecipeBuilder<?> backingBuilder;

    public CTRecipeBuilder(RecipeBuilder<?> backingBuilder) {
        this.backingBuilder = backingBuilder;
    }

    @ZenMethod
    public CTRecipeBuilder duration(int duration) {
        this.backingBuilder.duration(duration);
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder EUt(int EUt) {
        this.backingBuilder.EUt(EUt);
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder hidden() {
        this.backingBuilder.hidden();
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder inputs(IIngredient... ingredients) {
        this.backingBuilder.inputsIngredients(Arrays.stream(ingredients)
                .map(s -> {
                    IGTRecipeInput ri;
                    if (s instanceof IOreDictEntry) {
                        ri = GTRecipeOreInput.getOrCreate(((IOreDictEntry) s).getName(), s.getAmount());
                    } else {
                        ri = GTRecipeItemInput.getOrCreate(new CraftTweakerItemInputWrapper(s), s.getAmount());
                    }
                    return ri;
                }).collect(Collectors.toList()));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(IIngredient... ingredients) {
        this.backingBuilder.inputsIngredients(Arrays.stream(ingredients)
                .map(s -> {
                    IGTRecipeInput ri;
                    if (s instanceof IOreDictEntry) {
                        ri = GTRecipeOreInput.getOrCreate(((IOreDictEntry) s).getName(), s.getAmount()).setNonConsumable();
                    } else {
                        ri = GTRecipeItemInput.getOrCreate(new CraftTweakerItemInputWrapper(s), s.getAmount()).setNonConsumable();
                    }
                    return ri;
                }).collect(Collectors.toList()));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(ILiquidStack ingredient) {
        this.backingBuilder.notConsumable(CraftTweakerMC.getLiquidStack(ingredient));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder circuit(int num) {
        if (num < 0 || num > IntCircuitIngredient.CIRCUIT_MAX)
            CraftTweakerAPI.logError("Given configuration number is out of range!", new IllegalArgumentException());
        this.backingBuilder.notConsumable(new IntCircuitIngredient(num));
        return this;
    }

    //note that fluid input predicates are not supported
    @ZenMethod
    public CTRecipeBuilder fluidInputs(ILiquidStack... ingredients) {
        this.backingBuilder.fluidInputs(Arrays.stream(ingredients)
                .map(CraftTweakerMC::getLiquidStack).map(fluidStack -> GTRecipeFluidInput.getOrCreate(fluidStack, fluidStack.amount)).collect(Collectors.toList()));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder outputs(IItemStack... ingredients) {
        this.backingBuilder.outputs(Arrays.stream(ingredients)
                .map(CraftTweakerMC::getItemStack)
                .collect(Collectors.toList()));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder chancedOutput(IItemStack outputStack, int chanceValue, int tierChanceBoost) {
        this.backingBuilder.chancedOutput(CraftTweakerMC.getItemStack(outputStack), chanceValue, tierChanceBoost);
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder fluidOutputs(ILiquidStack... ingredients) {
        this.backingBuilder.fluidOutputs(Arrays.stream(ingredients)
                .map(CraftTweakerMC::getLiquidStack)
                .collect(Collectors.toList()));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, int value) {
        boolean applied = this.backingBuilder.applyProperty(key, value);
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName());
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, String value) {
        boolean applied = this.backingBuilder.applyProperty(key, value);
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName());
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, boolean value) {
        boolean applied = this.backingBuilder.applyProperty(key, value);
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName());
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, long value) {
        boolean applied = this.backingBuilder.applyProperty(key, value);
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName());
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, float value) {
        boolean applied = this.backingBuilder.applyProperty(key, value);
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName());
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder property(String key, IItemStack item) {
        boolean applied = this.backingBuilder.applyProperty(key, CraftTweakerMC.getItemStack(item));
        if (!applied) {
            throw new IllegalArgumentException("Property " +
                    key + " cannot be applied to recipe type " +
                    backingBuilder.getClass().getSimpleName() + " for Item " + CraftTweakerMC.getItemStack(item).getDisplayName());
        }
        return this;
    }

    @ZenMethod
    public void buildAndRegister() {
        this.backingBuilder.isCTRecipe().buildAndRegister();
    }

    @ZenMethod
    @Override
    public String toString() {
        return this.backingBuilder.toString();
    }

    public static class CraftTweakerItemInputWrapper extends GTRecipeItemInput {

        private final IIngredient ingredient;

        public CraftTweakerItemInputWrapper(IIngredient ingredient) {
            super(CraftTweakerMC.getItemStack(ingredient.getItems().get(0)));
            this.ingredient = ingredient;
        }

        @Override
        public boolean acceptsStack(@Nullable ItemStack itemStack) {
            if (itemStack == null) {
                return false;
            }
            itemStack = itemStack.copy();
            //because CT is dump enough to compare stack sizes by default...
            itemStack.setCount(ingredient.getAmount());
            return ingredient.matches(CraftTweakerMC.getIItemStack(itemStack));
        }
    }

}
