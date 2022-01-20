package gregtech.integration.jei.recipe;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.data.NBTConverter;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.Recipe.ChanceEntry;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.crafttweaker.CTUtilities;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.integration.jei.utils.AdvancedRecipeWrapper;
import gregtech.integration.jei.utils.JEIHelpers;
import gregtech.integration.jei.utils.JeiButton;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class GTRecipeWrapper extends AdvancedRecipeWrapper {

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
            List<ItemStack> recipeOutputs = recipe.getOutputs()
                    .stream().map(ItemStack::copy).collect(Collectors.toList());
            currentItemSlot += recipeOutputs.size();

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
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
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

    @Override
    public void initExtras() {
        buttons.add(new JeiButton(180, 2, 10, 10)
                .setTextures(GuiTextures.BUTTON_CLEAR_GRID)
                .setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                    outputRemoveLine();
                    return true;
                })
                .setActiveSupplier(() -> Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative() && GTValues.isModLoaded(GTValues.MODID_CT)));
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
        if (recipeMap == RecipeMaps.COKE_OVEN_RECIPES)
            return LINE_HEIGHT - 6; // fun hack TODO Make this easier to position
        return (recipe.getPropertyCount() + 3) * LINE_HEIGHT - 3;
    }

    private void outputRemoveLine() {
        StringBuilder builder = new StringBuilder();
        builder.append("<recipemap:")
                .append(recipeMap.unlocalizedName)
                .append(">.findRecipe(")
                .append(recipe.getEUt())
                .append(", ");

        if (recipe.getInputs().size() > 0) {
            builder.append("[");
            for (CountableIngredient ci : recipe.getInputs()) {
                ItemStack itemStack = null;
                String itemId = null;
                for (ItemStack item : ci.getIngredient().getMatchingStacks()) {
                    itemId = CTUtilities.getMetaItemId(item);
                    if (itemId != null) {
                        builder.append("<metaitem:")
                                .append(itemId)
                                .append(">");
                        itemStack = item;
                        break;
                    } else if (itemStack == null) {
                        itemStack = item;
                    }
                }
                if (itemStack != null) {
                    if (itemId == null) {
                        if (itemStack.getItem().getRegistryName() == null) {
                            GTLog.logger.info("Could not remove recipe {}, because of unregistered Item", builder);
                            return;
                        }
                        builder.append("<")
                                .append(itemStack.getItem().getRegistryName().toString())
                                .append(":")
                                .append(itemStack.getItemDamage())
                                .append(">");
                    }

                    if (itemStack.serializeNBT().hasKey("tag")) {
                        String nbt = NBTConverter.from(itemStack.serializeNBT().getCompoundTag("tag"), false).toString();
                        if (nbt.length() > 0) {
                            builder.append(".withTag(").append(nbt).append(")");
                        }
                    }
                }

                if (ci.getCount() != 1) {
                    builder.append(" * ")
                            .append(ci.getCount());
                }
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("], ");
        } else {
            builder.append("null, ");
        }

        if (recipe.getFluidInputs().size() > 0) {
            builder.append("[");
            for (FluidStack fluidStack : recipe.getFluidInputs()) {
                builder.append("<liquid:")
                        .append(fluidStack.getFluid().getName())
                        .append(">");

                if (fluidStack.amount != 1) {
                    builder.append(" * ")
                            .append(fluidStack.amount);
                }

                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length())
                    .append("]");
        } else {
            builder.append("null");
        }


        builder.append(").remove();");

        String output = "";
        if (recipe.getOutputs().size() > 0) {
            output = recipe.getOutputs().get(0).getDisplayName() + " * " + recipe.getOutputs().get(0).getCount();
        } else if (recipe.getFluidOutputs().size() > 0) {
            output = recipe.getFluidOutputs().get(0).getLocalizedName() + " * " + recipe.getFluidOutputs().get(0).amount;
        }

        if (!output.isEmpty()) {
            output = "// " + output + "\n";
        }

        ClipboardUtil.copyToClipboard(output + builder.toString() + "\n");
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Copied [\u00A76" + builder.toString() + "\u00A7r] to the clipboard"));
    }
}
