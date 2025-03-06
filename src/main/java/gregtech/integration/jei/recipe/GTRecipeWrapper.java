package gregtech.integration.jei.recipe;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.recipes.machines.IScannerRecipeMap;
import gregtech.api.recipes.output.FluidOutputProvider;
import gregtech.api.recipes.output.ItemOutputProvider;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.impl.ComputationProperty;
import gregtech.api.recipes.properties.impl.ScanProperty;
import gregtech.api.recipes.properties.impl.TotalComputationProperty;
import gregtech.api.recipes.roll.ListWithRollInformation;
import gregtech.api.recipes.roll.RollInterpreter;
import gregtech.api.recipes.roll.RollInterpreterApplication;
import gregtech.api.recipes.ui.JEIDisplayControl;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.integration.RecipeCompatUtil;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JeiButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class GTRecipeWrapper extends AdvancedRecipeWrapper {

    private static final int LINE_HEIGHT = 10;

    private final RecipeMap<?> recipeMap;
    private final Recipe recipe;

    private final ListWithRollInformation<GTItemIngredient> itemIngredients;
    private final ListWithRollInformation<GTFluidIngredient> fluidIngredients;

    private JEIDisplayControl itemInDisplayControl;
    private JEIDisplayControl fluidInDisplayControl;

    private final ItemOutputProvider itemOutputProvider;
    private final FluidOutputProvider fluidOutputProvider;

    public GTRecipeWrapper(RecipeMap<?> recipeMap, Recipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;
        this.itemIngredients = recipe.getItemIngredients();
        this.fluidIngredients = recipe.getFluidIngredients();
        this.itemOutputProvider = recipe.getItemOutputProvider();
        this.fluidOutputProvider = recipe.getFluidOutputProvider();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(@NotNull IIngredients ingredients) {
        // Inputs
        if (!itemIngredients.isEmpty()) {
            List<List<ItemStack>> list = new ArrayList<>();
            for (GTItemIngredient input : itemIngredients) {
                list.add(GTUtility.copyStackList(input.getAllMatchingStacks()));
            }
            ingredients.setInputLists(VanillaTypes.ITEM, list);
        }

        // Fluid Inputs
        if (!fluidIngredients.isEmpty()) {
            List<List<FluidStack>> list = new ArrayList<>();
            for (GTFluidIngredient input : fluidIngredients) {
                list.add(GTUtility.copyFluidList(input.getAllMatchingStacks()));
            }
            ingredients.setInputLists(VanillaTypes.FLUID, list);
        }

        // Outputs
        if (itemOutputProvider.getMaximumOutputs(1) > 0) {
            List<ItemStack> recipeOutputs = itemOutputProvider.getCompleteOutputs(1, recipeMap.getMaxOutputs())
                    .stream().map(ItemStack::copy)
                    .collect(Collectors.toList());

            List<ItemStack> scannerPossibilities = null;
            if (this.recipeMap instanceof IScannerRecipeMap) {
                scannerPossibilities = new ArrayList<>();
                // Scanner Output replacing, used for cycling research outputs
                String researchId = null;
                for (ItemStack stack : recipe.getOutputs()) {
                    researchId = AssemblyLineManager.readResearchId(stack);
                    if (researchId != null) break;
                }
                if (researchId != null) {
                    Collection<Recipe> possibleRecipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                            .getDataStickEntry(researchId);
                    if (possibleRecipes != null) {
                        for (Recipe r : possibleRecipes) {
                            ItemStack researchItem = r.getOutputs().get(0);
                            researchItem = researchItem.copy();
                            researchItem.setCount(1);
                            boolean didMatch = false;
                            for (ItemStack stack : scannerPossibilities) {
                                if (ItemStack.areItemStacksEqual(stack, researchItem)) {
                                    didMatch = true;
                                    break;
                                }
                            }
                            if (!didMatch) scannerPossibilities.add(researchItem);
                        }
                    }
                    scannerPossibilities.add(recipeOutputs.get(0));
                }
            }

            if (scannerPossibilities == null || scannerPossibilities.isEmpty()) {
                ingredients.setOutputs(VanillaTypes.ITEM, recipeOutputs);
            } else {
                ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(scannerPossibilities));
            }
        }

        // Fluid Outputs
        if (fluidOutputProvider.getMaximumOutputs(1) > 0) {
            List<FluidStack> recipeOutputs = fluidOutputProvider.getCompleteOutputs(1, recipeMap.getMaxFluidOutputs())
                    .stream().map(FluidStack::copy)
                    .collect(Collectors.toList());

            ingredients.setOutputs(VanillaTypes.FLUID, recipeOutputs);
        }
    }

    public JEIDisplayControl getItemInDisplayControl() {
        if (itemInDisplayControl != null) return itemInDisplayControl;
        return itemInDisplayControl = new JEIDisplayControl() {

            @Override
            public @Nullable String addSmallDisplay(int index) {
                if (itemIngredients.isRolled(index)) {
                    return itemIngredients.getInterpreter().interpretSmallDisplay(index,
                            RollInterpreterApplication.ITEM_INPUT,
                            itemIngredients.getMaxYield(index), itemIngredients.getRollValue(index),
                            itemIngredients.getRollBoost(index));
                }
                return null;
            }
        };
    }

    public JEIDisplayControl getItemOutDisplayControl() {
        return itemOutputProvider;
    }

    public JEIDisplayControl getFluidInDisplayControl() {
        if (fluidInDisplayControl != null) return fluidInDisplayControl;
        return fluidInDisplayControl = new JEIDisplayControl() {

            @Override
            public @Nullable String addSmallDisplay(int index) {
                if (fluidIngredients.isRolled(index)) {
                    return fluidIngredients.getInterpreter().interpretSmallDisplay(index,
                            RollInterpreterApplication.FLUID_INPUT,
                            fluidIngredients.getMaxYield(index), fluidIngredients.getRollValue(index),
                            fluidIngredients.getRollBoost(index));
                }
                return null;
            }
        };
    }

    public JEIDisplayControl getFluidOutDisplayControl() {
        return fluidOutputProvider;
    }

    public void addItemTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (input) {
            RollInterpreter interpreter = itemIngredients.getInterpreter();
            tooltip.add(interpreter.interpretTooltip(slotIndex, RollInterpreterApplication.ITEM_INPUT,
                    itemIngredients.getMaxYield(slotIndex), itemIngredients.getRollValue(slotIndex),
                    itemIngredients.getRollBoost(slotIndex)));
        } else {
            // tooltip.add(recipe.getItemOutputProvider().addTooltip(slotIndex - recipeMap.getMaxInputs()));

            if (this.recipeMap instanceof IScannerRecipeMap && !ingredient.isEmpty()) {
                // check for "normal" data items
                if (ingredient.getItem() instanceof IDataItem) return;
                // check for metaitem data items
                if (ingredient.getItem() instanceof MetaItem<?>metaItem) {
                    for (IItemBehaviour behaviour : metaItem.getBehaviours(ingredient)) {
                        if (behaviour instanceof IDataItem) {
                            return;
                        }
                    }
                }
                // If we are here, we know this is not the data item, so add the tooltip
                if (recipe.hasProperty(ScanProperty.getInstance())) {
                    tooltip.add(TooltipHelper.BLINKING_CYAN + I18n.format("gregtech.recipe.research_result"));
                }
            }
        }
    }

    public void addFluidTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
        TankWidget.addIngotMolFluidTooltip(ingredient, tooltip);

        if (input) {
            RollInterpreter interpreter = fluidIngredients.getInterpreter();
            tooltip.add(interpreter.interpretTooltip(slotIndex, RollInterpreterApplication.FLUID_INPUT,
                    fluidIngredients.getMaxYield(slotIndex), fluidIngredients.getRollValue(slotIndex),
                    fluidIngredients.getRollBoost(slotIndex)));
        } else {
            // tooltip.add(recipe.getFluidOutputProvider().addTooltip(slotIndex - recipeMap.getMaxFluidInputs()));
        }
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        var storage = recipe.propertyStorage();
        var properties = storage.values();
        boolean drawTotalEU = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideTotalEU);
        boolean drawEUt = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideEUt);
        boolean drawDuration = properties.isEmpty() || properties.stream().noneMatch(RecipeProperty::hideDuration);

        int defaultLines = 0;
        if (drawTotalEU) defaultLines++;
        if (drawEUt) defaultLines++;
        if (drawDuration) defaultLines++;

        int unhiddenCount = (int) storage.entrySet().stream()
                .filter((property) -> !property.getKey().isHidden())
                .count();
        int yPosition = recipeHeight - ((unhiddenCount + defaultLines) * 10 - 3);

        // Default entries
        if (drawTotalEU) {
            long eu = recipe.getVoltage() * recipe.getDuration();
            // sadly we still need a custom override here, since computation uses duration and EU/t very differently
            if (storage.contains(TotalComputationProperty.getInstance()) &&
                    storage.contains(ComputationProperty.getInstance())) {
                int minimumCWUt = storage.get(ComputationProperty.getInstance(), 1);
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.max_eu", eu / minimumCWUt), 0, yPosition,
                        0x111111);
            } else {
                minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total", eu), 0, yPosition, 0x111111);
            }
        }
        if (drawEUt) {
            minecraft.fontRenderer.drawString(
                    I18n.format(
                            recipeMap.getRecipeMapUI().isGenerator() ? "gregtech.recipe.eu_inverted" :
                                    "gregtech.recipe.eu",
                            recipe.getVoltage(), GTValues.VN[GTUtility.getTierByVoltage(recipe.getVoltage())]),
                    0, yPosition += LINE_HEIGHT, 0x111111);
        }
        if (drawDuration) {
            minecraft.fontRenderer.drawString(
                    I18n.format("gregtech.recipe.duration",
                            TextFormattingUtil.formatNumbers(recipe.getDuration() / 20.0)),
                    0, yPosition += LINE_HEIGHT, 0x111111);
        }

        // Property custom entries
        for (var propertyEntry : storage.entrySet()) {
            if (!propertyEntry.getKey().isHidden()) {
                RecipeProperty<?> property = propertyEntry.getKey();
                var value = propertyEntry.getValue();
                property.drawInfo(minecraft, 0, yPosition += property.getInfoHeight(value), 0x111111, value, mouseX,
                        mouseY);
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltips = new ArrayList<>();
        for (var entry : recipe.propertyStorage().entrySet()) {
            if (!entry.getKey().isHidden()) {
                RecipeProperty<?> property = entry.getKey();
                Object value = entry.getValue();
                property.getTooltipStrings(tooltips, mouseX, mouseY, value);
            }
        }
        return tooltips;
    }

    @Override
    public void initExtras() {
        // do not add the info or X button if no tweaker mod is present
        if (!RecipeCompatUtil.isTweakerLoaded()) return;

        BooleanSupplier creativePlayerPredicate = () -> Minecraft.getMinecraft().player != null &&
                Minecraft.getMinecraft().player.isCreative();
        BooleanSupplier creativeTweaker = () -> creativePlayerPredicate.getAsBoolean() &&
                (recipe.isGroovyRecipe());
        BooleanSupplier creativeDefault = () -> creativePlayerPredicate.getAsBoolean() &&
                !recipe.isGroovyRecipe();

        // X Button
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipBuilder(lines -> lines.add(
                        LocalizationUtils.format("gregtech.jei.remove_recipe.tooltip",
                                RecipeCompatUtil.getTweakerName())))
                .setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                    String recipeLine = RecipeCompatUtil.getRecipeRemoveLine(recipeMap, recipe);
                    String output = RecipeCompatUtil.getFirstOutputString(recipe);
                    if (!output.isEmpty()) {
                        output = "// " + output + "\n";
                    }
                    String copyString = output + recipeLine + "\n";
                    ClipboardUtil.copyToClipboard(copyString);
                    Minecraft.getMinecraft().player.sendMessage(
                            new TextComponentString("Copied [\u00A76" + recipeLine + "\u00A7r] to the clipboard"));
                    return true;
                })
                .setActiveSupplier(creativeDefault));

        // CT/GS Info
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.INFO_ICON)
                .setTooltipBuilder(lines -> lines.add(recipe.isGroovyRecipe() ?
                        LocalizationUtils.format("gregtech.jei.gs_recipe.tooltip") :
                        LocalizationUtils.format("gregtech.jei.ct_recipe.tooltip")))
                .setClickAction((mc, x, y, button) -> false)
                .setActiveSupplier(creativeTweaker));
    }
}
