package gregtech.common.command.util;

import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.common.items.MetaItems;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandRecipeCheck extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "recipecheck";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "gregtech.command.util.recipecheck.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        sender.sendMessage(new TextComponentTranslation("gregtech.command.util.recipecheck.begin"));

        List<MismatchEntry> mismatchedRecipes = new ArrayList<>();

        GTLog.logger.info("[Recipe Checker] Starting recipe conflict check...");
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            GTLog.logger.info("[Recipe Checker] Checking recipe map " + recipeMap.getUnlocalizedName());
            GTLog.logger.info("[Recipe Checker] Iterating over " +recipeMap.getRecipeList().size() + " recipes");
            for (Recipe recipe : recipeMap.getRecipeList()) {
                MismatchEntry checkResult = checkRecipe(
                        recipe,
                        recipeMap,
                        recipe.getFluidInputs()
                                .stream().map(stack -> new FluidStack(stack, stack.amount * 10000))
                                .collect(Collectors.toList()),
                        recipe.getInputs(), 0);
                if (checkResult != null) {
                    mismatchedRecipes.add(checkResult);
                }
            }
        }

        GTLog.logger.info("[Recipe Checker] Completed recipe check!");
        GTLog.logger.info("[Recipe Checker] Found " + mismatchedRecipes.size() + " potential conflicts");
        for (MismatchEntry mismatch : mismatchedRecipes) {
            GTLog.logger.error(
                    "\nIn map " + mismatch.recipeMap.getUnlocalizedName() +
                    "\nTried: " + prettyPrintRecipe(mismatch.attempted) +
                    "\nFound: " + prettyPrintRecipe(mismatch.found)
            );
        }

        sender.sendMessage(new TextComponentTranslation("gregtech.command.util.recipecheck.end", mismatchedRecipes.size()));
    }

    /**
     * Recursively check all combinations of item inputs until one is mismatched or there are no more
     */
    private MismatchEntry checkRecipe(Recipe recipe, RecipeMap<?> recipeMap, List<FluidStack> fluidInputs, List<CountableIngredient> itemInputs, int startIndex) {
        // if we have not yet bottomed out the input list
        if (startIndex < itemInputs.size()) {
            CountableIngredient ingredient = itemInputs.get(startIndex);
            ItemStack[] matchingStacks = ingredient.getIngredient().getMatchingStacks();
            if (matchingStacks.length > 1) {
                // check each possible input for this level
                for (ItemStack stack : matchingStacks) {
                    // shallow copy the input list to avoid editing it
                    List<CountableIngredient> reducedIngredients = new ArrayList<>(itemInputs);
                    // reduce the current level to only the current ItemStack
                    reducedIngredients.set(startIndex, CountableIngredient.from(stack, ingredient.getCount()));
                    // go one level deeper into the input list
                    MismatchEntry checkResult = checkRecipe(recipe, recipeMap, fluidInputs, reducedIngredients, startIndex + 1);
                    // only trigger the return chain if we found a mismatch, we want to continue the loop otherwise
                    if (checkResult != null){
                        return checkResult;
                    }
                }
            }
            else {
                // go one level deeper into the input list
                return checkRecipe(recipe, recipeMap, fluidInputs, itemInputs, startIndex + 1);
            }
        }
        else {
            // actually check the recipe
            Recipe foundRecipe = recipeMap.findRecipe(Long.MAX_VALUE, recipe.getInputs().stream().map(ingredient -> {
                        ItemStack[] stacks = ingredient.getIngredient().getMatchingStacks();
                        if (stacks.length > 0) {
                            ItemStack outStack = stacks[0].copy();
                            outStack.setCount(Math.max(1, ingredient.getCount()) * 100);
                            return outStack;
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList()),
                    fluidInputs, Integer.MAX_VALUE, MatchingMode.DEFAULT);
            if (foundRecipe != recipe) {
                return new MismatchEntry(recipe, foundRecipe, recipeMap);
            }
            return null;
        }
        // only hit when the stack reduction loop finishes without finding a conflict
        return null;
    }

    public String prettyPrintRecipe(Recipe recipe) {
        if (recipe == null) {
            return "null (Is something else going wrong?)\n";
        }
        StringBuilder output = new StringBuilder();
        output.append("EU/t: ")
                .append(recipe.getEUt())
                .append(", Duration: ")
                .append(recipe.getDuration());
        if (recipe.isHidden()) {
            output.append(", hidden");
        }
        output.append("\n");

        if (recipe.getInputs().size() > 0) {
            output.append("Item inputs:\n");
            for (CountableIngredient ingredient : recipe.getInputs()) {
                output.append("    ")
                        .append(prettyPrintCountableIngredient(ingredient))
                        .append("\n");
            }
        }

        if (recipe.getFluidInputs().size() > 0) {
            output.append("Fluid inputs:\n");
            for (FluidStack fluid : recipe.getFluidInputs()) {
                output.append("    ")
                        .append(fluid.getUnlocalizedName())
                        .append(" * ")
                        .append(fluid.amount)
                        .append("\n");
            }
        }

        if (recipe.getOutputs().size() > 0) {
            output.append("Item outputs:\n");
            for (ItemStack stack : recipe.getOutputs()) {
                output.append("    ")
                        .append(prettyPrintItemStack(stack))
                        .append("\n");
            }
        }

        if (recipe.getChancedOutputs().size() > 0) {
            output.append("Item chanced outputs:\n");
            for (Recipe.ChanceEntry chanceEntry : recipe.getChancedOutputs()) {
                output.append("    ")
                        .append(prettyPrintItemStack(chanceEntry.getItemStack()))
                        .append(" (Chance: ")
                        .append(chanceEntry.getChance())
                        .append(", Boost: ")
                        .append(chanceEntry.getBoostPerTier())
                        .append(")\n");
            }
        }

        if (recipe.getFluidOutputs().size() > 0) {
            output.append("Fluid outputs:\n");
            for (FluidStack fluid : recipe.getFluidOutputs()) {
                output.append("    ")
                        .append(fluid.getUnlocalizedName())
                        .append(" * ")
                        .append(fluid.amount)
                        .append("\n");
            }
        }

        return output.toString();
    }

    public String prettyPrintCountableIngredient(CountableIngredient countableIngredient) {
        StringBuilder output = new StringBuilder();
        if (countableIngredient.getIngredient() instanceof OreIngredient) {
            output.append("(OreDict) ");
        }
        output.append("{");
        for (ItemStack stack : countableIngredient.getIngredient().getMatchingStacks()) {
            output.append(" ")
                    .append(prettyPrintItemStack(stack));
        }
        output.append(" } * ")
                .append(countableIngredient.getCount());
        return output.toString();
    }

    public String prettyPrintItemStack(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) stack.getItem();
            MetaValueItem metaValueItem = metaItem.getItem(stack);
            if (metaValueItem == null) {
                if (metaItem instanceof MetaPrefixItem) {
                    Material material = ((MetaPrefixItem) metaItem).getMaterial(stack);
                    OrePrefix orePrefix = ((MetaPrefixItem) metaItem).getOrePrefix();
                    return "(MetaItem) OrePrefix: " + orePrefix.name + ", Material: " + material;
                }
            } else {
                if (MetaItems.INTEGRATED_CIRCUIT.isItemEqual(stack)) {
                    return "Config circuit #" + IntCircuitIngredient.getCircuitConfiguration(stack);
                }
                return "(MetaItem) " + metaValueItem.unlocalizedName;
            }
        }
        //noinspection ConstantConditions
        return stack.getItem().getRegistryName().toString() + " * " + stack.getCount() + " (Meta " + stack.getItemDamage() + ")";
    }

    private static class MismatchEntry {
        public Recipe attempted;
        public Recipe found;
        public RecipeMap<?> recipeMap;

        public MismatchEntry(Recipe attempted, Recipe found, RecipeMap<?> recipeMap) {
            this.attempted = attempted;
            this.found = found;
            this.recipeMap = recipeMap;
        }
    }
}
