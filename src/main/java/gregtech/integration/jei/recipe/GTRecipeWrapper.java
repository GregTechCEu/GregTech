package gregtech.integration.jei.recipe;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.Recipe.ChanceEntry;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.integration.jei.utils.JEIHelpers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class GTRecipeWrapper implements IRecipeWrapper {

    private static final int LINE_HEIGHT = 10;

    private final Int2ObjectMap<ChanceEntry> chanceOutput = new Int2ObjectOpenHashMap<>();
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
        int currentSlot = recipeMap.getMaxInputs();

        // Inputs
        if (!recipe.getInputs().isEmpty()) {
            List<List<ItemStack>> matchingInputs = new ArrayList<>(recipe.getInputs().size());
            recipe.getInputs().forEach(ci -> matchingInputs.add(Arrays.stream(ci.getIngredient().getMatchingStacks())
                    .sorted(OreDictUnifier.getItemStackComparator())
                    .map(is -> GTUtility.copyAmount(ci.getCount() == 0 ? 1 : ci.getCount(), is))
                    .collect(Collectors.toList())));
            ingredients.setInputLists(VanillaTypes.ITEM, matchingInputs);
        }

        // Fluid Inputs
        if (!recipe.getFluidInputs().isEmpty()) {
            ingredients.setInputs(VanillaTypes.FLUID, recipe.getFluidInputs().stream()
                    .map(fs -> GTUtility.copyAmount(fs.amount == 0 ? 1 : fs.amount, fs))
                    .collect(Collectors.toList()));
        }

        // Outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().isEmpty()) {
            List<ItemStack> recipeOutputs = recipe.getOutputs()
                    .stream().map(ItemStack::copy).collect(Collectors.toList());
            currentSlot += recipeOutputs.size();

            List<ChanceEntry> chancedOutputs = recipe.getChancedOutputs();
            chancedOutputs.sort(Comparator.comparingInt(entry -> entry == null ? 0 : entry.getChance()));
            for (ChanceEntry chancedEntry : chancedOutputs) {
                chanceOutput.put(currentSlot++, chancedEntry);
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

    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        boolean notConsumed = input && recipe.isNotConsumedInput(ingredient);
        ChanceEntry entry = input ? null : chanceOutput.get(slotIndex);

        if (entry != null) {
            double chance = entry.getChance() / 100.0;
            double boost = entry.getBoostPerTier() / 100.0;
            tooltip.add(I18n.format("gregtech.recipe.chance", chance, boost));
        } else if (notConsumed) {
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

    private int getPropertyListHeight() {
        return (recipe.getPropertyCount() + 3) * LINE_HEIGHT - 3;
    }
}
