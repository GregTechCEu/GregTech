package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        if (ingredient.getItems().isEmpty()) {
            if (oreDict != null) {
                throw new IllegalArgumentException("Invalid Ore Dictionary [" + oreDict + "]: contains no items");
            } else {
                throw new IllegalArgumentException("Invalid Item [" + ingredient + "]: item not found");
            }
        }
    }

    @ZenMethod
    public CTRecipeBuilder inputs(@Nonnull IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            this.backingBuilder.input(getInputFromCTIngredient(ingredient));
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(@Nonnull IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            this.backingBuilder.notConsumable(getInputFromCTIngredient(ingredient));
        }
        return this;
    }

    @Nonnull
    private static GTRecipeInput getInputFromCTIngredient(@Nullable IIngredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Invalid ingredient: is null");
        }

        final List<IItemStack> items = ingredient.getItems();
        final String oreDict = extractOreDictEntry(ingredient);
        if (oreDict != null) {
            // ore dict
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Invalid Ore Dictionary [" + oreDict + "]: contains no items");
            }
            return GTRecipeOreInput.getOrCreate(oreDict, ingredient.getAmount());
        } else if (items.isEmpty()) {
            // no possible input from what was supplied
            throw new IllegalArgumentException("Invalid Item [" + ingredient + "]: item not found");
        } else if (items.size() == 1) {
            // single input
            ItemStack stack = CraftTweakerMC.getItemStack(items.get(0));
            GTRecipeInput input = GTRecipeItemInput.getOrCreate(stack, ingredient.getAmount());

            // nbt support, if a tag is present
            final NBTTagCompound compound = CraftTweakerMC.getNBTCompound(items.get(0).getTag());
            if (compound != null) {
                final Set<Map.Entry<String, NBTBase>> entrySet = compound.tagMap.entrySet();
                return input.setNBTMatchingCondition((tag, ignored) -> {
                    // return if the tag to check has everything the recipe requires
                    return tag.tagMap.entrySet().containsAll(entrySet);
                }, null);
            }
            return input;
        } else {
            // multiple inputs for a single input entry
            ItemStack[] itemStacks = new ItemStack[items.size()];
            for (int i = 0; i < itemStacks.length; i++) {
                itemStacks[i] = CraftTweakerMC.getItemStack(ingredient.getItems().get(i));
            }
            return GTRecipeItemInput.getOrCreate(itemStacks);
        }
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(ILiquidStack ingredient) {
        this.backingBuilder.notConsumable(CraftTweakerMC.getLiquidStack(ingredient));
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder circuit(int num) {
        this.backingBuilder.circuitMeta(num);
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
