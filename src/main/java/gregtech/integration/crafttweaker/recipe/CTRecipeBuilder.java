package gregtech.integration.crafttweaker.recipe;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.data.DataMap;
import crafttweaker.api.data.IData;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @ZenMethod
    public CTRecipeBuilder inputs(@NotNull IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            this.backingBuilder.input(getInputFromCTIngredient(ingredient));
        }
        return this;
    }

    @ZenMethod
    public CTRecipeBuilder notConsumable(@NotNull IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            this.backingBuilder.notConsumable(getInputFromCTIngredient(ingredient));
        }
        return this;
    }

    @NotNull
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
            return new GTRecipeOreInput(oreDict, ingredient.getAmount());
        } else if (items.isEmpty()) {
            // no possible input from what was supplied
            throw new IllegalArgumentException("Invalid Item [" + ingredient + "]: item not found");
        } else if (items.size() == 1) {
            // single input
            final ItemStack stack = CraftTweakerMC.getItemStack(items.get(0));
            final IData data = items.get(0).getTag();
            // MCItemStack#getTag returns DataMap.EMPTY when there is no tag, instead of null
            // CraftTweakerMC#getNBTCompound does not check for this, so it would otherwise return an empty NBT tag
            // check for the empty tag specifically, so it is treated as a non-nbt input instead
            final NBTTagCompound tagCompound = data == DataMap.EMPTY ? null : CraftTweakerMC.getNBTCompound(data);

            return tryConstructNBTInput(new GTRecipeItemInput(stack, ingredient.getAmount()), tagCompound);
        } else {
            // multiple inputs for a single input entry
            final Map<ItemStack, List<NBTTagCompound>> map = new Object2ObjectOpenCustomHashMap<>(
                    ItemStackHashStrategy.comparingItemDamageCount());

            ItemStack[] stacks = new ItemStack[items.size()];
            for (int i = 0; i < stacks.length; i++) {
                IItemStack item = items.get(i);
                final ItemStack stack = CraftTweakerMC.getItemStack(item);
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Item [" + ingredient + "]: contains empty ItemStack.");
                }
                stacks[i] = stack;

                final NBTTagCompound compound = CraftTweakerMC.getNBTCompound(item.getTag());
                if (compound != null) {
                    if (map.containsKey(stack)) {
                        map.get(stack).add(compound);
                    } else {
                        List<NBTTagCompound> list = new ArrayList<>(1);
                        list.add(compound);
                        map.put(stack, list);
                    }
                }
            }

            return tryConstructNBTInput(new GTRecipeItemInput(stacks), map);
        }
    }

    /**
     * Attempt to construct an NBT matcher for matching a single tag compound
     *
     * @param input    the base recipe input
     * @param compound the nbt compound to match
     * @return the nbt matching input if successful, otherwise the original recipe input
     */
    @NotNull
    private static GTRecipeInput tryConstructNBTInput(@NotNull GTRecipeInput input, @Nullable NBTTagCompound compound) {
        if (compound == null) return input; // do not use nbt matching, if there is no tag to check
        if (compound.isEmpty()) {
            // special case which considers an empty nbt tag as allowing any or no NBT
            return input.setNBTMatchingCondition(NBTMatcher.ANY, NBTCondition.ANY);
        }
        return input.setNBTMatchingCondition(new CTNBTMatcher(compound), null);
    }

    /**
     * Attempt to construct an NBT matcher for matching multiple item stacks to their respective compounds
     *
     * @param input the base recipe input
     * @param map   a mapping of stacks to compounds. The map's key hashing should ignore NBT compounds
     * @return the nbt matching input if successful, otherwise the original recipe input
     */
    @NotNull
    private static GTRecipeInput tryConstructNBTInput(@NotNull GTRecipeInput input,
                                                      @NotNull Map<ItemStack, List<NBTTagCompound>> map) {
        if (map.isEmpty()) return input; // do not use nbt matching, if there are no tags to check
        return input.setNBTMatchingCondition(new CTNBTMultiItemMatcher(map), null);
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

    // note that fluid input predicates are not supported
    @ZenMethod
    public CTRecipeBuilder fluidInputs(ILiquidStack... ingredients) {
        this.backingBuilder.fluidInputs(Arrays.stream(ingredients)
                .map(CraftTweakerMC::getLiquidStack)
                .map(GTRecipeFluidInput::new)
                .collect(Collectors.toList()));
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
                    backingBuilder.getClass().getSimpleName() + " for Item " +
                    CraftTweakerMC.getItemStack(item).getDisplayName());
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
