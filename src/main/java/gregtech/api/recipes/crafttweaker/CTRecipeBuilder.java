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

    // TODO YEET

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

    private static String extractOreDictEntry(IIngredient ingredient) {
        if (ingredient instanceof IOreDictEntry)
            return ((IOreDictEntry) ingredient).getName();
        if (ingredient.getInternal() instanceof IOreDictEntry)
            return ((IOreDictEntry) ingredient.getInternal()).getName();
        return null;
    }
    
    private static void checkIfExists(IIngredient ingredient, String oreDict) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Invalid ingredient: is null");
        }

        if (ingredient.getItems().size() == 0) {
            if (oreDict != null) {
                throw new IllegalArgumentException("Invalid Ore Dictionary [" + oreDict + "]: contains no items");
            } else {
                throw new IllegalArgumentException("Invalid Item [" + ingredient.toString() + "]: item not found");
            }
        }
    }

    @ZenMethod
    public CTRecipeBuilder inputs(IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            String oreDict = extractOreDictEntry(ingredient);
            checkIfExists(ingredient, oreDict);

            if (oreDict != null) {
                this.backingBuilder.input(
                        GTRecipeOreInput.getOrCreate(oreDict, ingredient.getAmount()));
            } else {
                this.backingBuilder.input(CraftTweakerItemInputWrapper.getOrCreate(ingredient, ingredient.getAmount()));
            }
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            String oreDict = extractOreDictEntry(ingredient);
            checkIfExists(ingredient, oreDict);

            if (oreDict != null) {
                this.backingBuilder.input(
                        GTRecipeOreInput.getOrCreate(oreDict, ingredient.getAmount())
                                .setNonConsumable());
            } else {
                this.backingBuilder.input(CraftTweakerItemInputWrapper.getOrCreate(
                                ingredient, ingredient.getAmount())
                        .setNonConsumable());
            }
        }
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

}
