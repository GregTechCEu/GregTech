package gregtech.integration.jei.recipe;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.Recipe.ChanceEntry;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.integration.jei.utils.JEIHelpers;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class GTRecipeWrapper implements IRecipeWrapper {

    private static final int LINE_HEIGHT = 10;

    private final Int2ObjectMap<ChanceEntry> chanceOutput = new Int2ObjectOpenHashMap<>();
    private final Int2BooleanMap notConsumedItemInput = new Int2BooleanOpenHashMap();
    private final Int2BooleanMap notConsumedFluidInput = new Int2BooleanOpenHashMap();
    private final RecipeMap<?> recipeMap;
    private final Recipe recipe;

    public GTRecipeWrapper(RecipeMap<?> recipeMap, Recipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        int currentItemSlot = 0;
        int currentFluidSlot = 0;

        // Inputs
        if (!recipe.getInputs().isEmpty()) {
            List<List<ItemStack>> matchingInputs = new ArrayList<>(recipe.getInputs().size());
            for (CountableIngredient ci : recipe.getInputs()) {
                matchingInputs.add(Arrays.stream(ci.getIngredient().getMatchingStacks())
                        .sorted(OreDictUnifier.getItemStackComparator())
                        .map(is -> GTUtility.copyAmount(ci.getCount() == 0 ? 1 : ci.getCount(), is))
                        .collect(Collectors.toList()));
                notConsumedItemInput.put(currentItemSlot++, ci.getCount() == 0);
            }
            ingredients.setInputLists(VanillaTypes.ITEM, matchingInputs);
        }
        currentItemSlot = recipeMap.getMaxInputs();

        // Fluid Inputs
        if (!recipe.getFluidInputs().isEmpty()) {
            ingredients.setInputs(VanillaTypes.FLUID, recipe.getFluidInputs().stream()
                    .map(fs -> GTUtility.copyAmount(fs.amount == 0 ? 1 : fs.amount, fs))
                    .collect(Collectors.toList()));
            for (FluidStack fs : recipe.getFluidInputs()) {
                notConsumedFluidInput.put(currentFluidSlot++, fs.amount == 0);
            }
        }

        // Outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().isEmpty()) {
            List<ItemStack> recipeOutputs;

            // Scanner Output replacing
            if (recipe.getOutputs().get(0).hasTagCompound() && recipe.getOutputs().get(0).isItemEqual(MetaItems.TOOL_DATA_STICK.getStackForm(1))) {
                ItemStack dataStick = recipe.getOutputs().get(0);
                NBTTagCompound researchItemNBT = dataStick.getSubCompound("asslineOutput");
                ItemStack researchItem = new ItemStack(researchItemNBT);
                if (researchItem != null) {
                    recipeOutputs = Collections.singletonList(researchItem);
                } else {
                    recipeOutputs = recipe.getOutputs()
                            .stream().map(ItemStack::copy).collect(Collectors.toList());
                    currentItemSlot += recipeOutputs.size();
                }

            } else {
                recipeOutputs = recipe.getOutputs()
                        .stream().map(ItemStack::copy).collect(Collectors.toList());
                currentItemSlot += recipeOutputs.size();
            }

            List<ChanceEntry> chancedOutputs = recipe.getChancedOutputs();
            chancedOutputs.sort(Comparator.comparingInt(entry -> entry == null ? 0 : entry.getChance()));
            for (ChanceEntry chancedEntry : chancedOutputs) {
                chanceOutput.put(currentItemSlot++, chancedEntry);
                recipeOutputs.add(chancedEntry.getItemStack());
            }
            ingredients.setOutputs(VanillaTypes.ITEM, recipeOutputs);
        }

        // Fluid Outputs
        if (!recipe.getFluidOutputs().isEmpty()) {
            ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidOutputs().stream()
                    .map(FluidStack::copy)
                    .collect(Collectors.toList()));
        }
    }

    public void addItemTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        boolean notConsumed = input && recipe.isNotConsumedInput(ingredient);
        ChanceEntry entry = input ? null : chanceOutput.get(slotIndex);

        if (entry != null) {
            double chance = entry.getChance() / 100.0;
            double boost = entry.getBoostPerTier() / 100.0;
            tooltip.add(I18n.format("gregtech.recipe.chance", chance, boost));
        } else if (notConsumed) {
            tooltip.add(I18n.format("gregtech.recipe.not_consumed"));
        } else if (recipe.getOutputs().get(0).hasTagCompound() && ((ItemStack) ingredient).isItemEqual(new ItemStack(recipe.getOutputs().get(0).getSubCompound("asslineOutput")))) {
            tooltip.add(I18n.format("gregtech.recipe.assline_scanner"));
        }
    }

    public void addFluidTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        boolean notConsumed = input && recipe.isNotConsumedInput(ingredient);

        if (notConsumed) {
            tooltip.add(I18n.format("gregtech.recipe.not_consumed"));
        }
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int yPosition = recipeHeight - getPropertyListHeight();
        if (!recipe.hasProperty(PrimitiveProperty.getInstance())) {
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total", Math.abs((long) recipe.getEUt()) * recipe.getDuration()), 0, yPosition, 0x111111);
            minecraft.fontRenderer.drawString(I18n.format(recipe.getEUt() >= 0 ? "gregtech.recipe.eu" : "gregtech.recipe.eu_inverted", Math.abs(recipe.getEUt()), JEIHelpers.getMinTierForVoltage(recipe.getEUt())), 0, yPosition += LINE_HEIGHT, 0x111111);
        } else yPosition -= LINE_HEIGHT * 2;
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.duration", recipe.getDuration() / 20f), 0, yPosition += LINE_HEIGHT, 0x111111);
        for (Map.Entry<RecipeProperty<?>, Object> propertyEntry : recipe.getPropertyValues()) {
            if (!propertyEntry.getKey().isHidden()) {
                propertyEntry.getKey().drawInfo(minecraft, 0, yPosition += LINE_HEIGHT, 0x111111, propertyEntry.getValue());
            }
        }
    }

    public Int2ObjectMap<ChanceEntry> getChanceOutputMap() {
        return chanceOutput;
    }

    public Int2BooleanMap getNotConsumedItemInputs() {
        return notConsumedItemInput;
    }

    public Int2BooleanMap getNotConsumedFluidInputs() {
        return notConsumedFluidInput;
    }

    private int getPropertyListHeight() {
        if (recipeMap == RecipeMaps.COKE_OVEN_RECIPES) return LINE_HEIGHT - 6; // fun hack TODO Make this easier to position
        return (recipe.getPropertyCount() + 3) * LINE_HEIGHT - 3;
    }
}
