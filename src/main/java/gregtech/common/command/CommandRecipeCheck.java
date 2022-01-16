package gregtech.common.command.util;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTLog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

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

        List<Pair<Recipe, Recipe>> mismatchedRecipes = new ArrayList<>();

        GTLog.logger.info("[Recipe Checker] Starting recipe conflict check...");
        for (RecipeMap<?> recipeMap : RecipeMap.getRecipeMaps()) {
            GTLog.logger.info("[Recipe Checker] Checking recipe map " + recipeMap.getUnlocalizedName());
            GTLog.logger.info("[Recipe Checker] Iterating over " +recipeMap.getRecipeList().size() + " recipes");
            for (Recipe recipe : recipeMap.getRecipeList()) {
            //if (recipeMap.getRecipeList().size() > 0) {Recipe recipe = (Recipe) recipeMap.getRecipeList().toArray()[0];
                /*GTLog.logger.info("[Recipe Checker] Check recipe:");
                for (CountableIngredient stack : recipe.getInputs()) {
                    GTLog.logger.info(stack.toPrettyString());
                }
                for (FluidStack stack : recipe.getFluidInputs()) {
                    GTLog.logger.info(stack.getUnlocalizedName() + " * " + stack.amount);
                }
                GTLog.logger.info("outputs:");
                for (ItemStack stack : recipe.getOutputs()) {
                    GTLog.logger.info(stack.toString());
                }
                for (Recipe.ChanceEntry stack : recipe.getChancedOutputs()) {
                    GTLog.logger.info(stack.getItemStack().toString());
                }
                for (FluidStack stack : recipe.getFluidOutputs()) {
                    GTLog.logger.info(stack.getUnlocalizedName() + " * " + stack.amount);
                }
                GTLog.logger.info("[Recipe Checker] Testing inputs...");*/
                Recipe foundRecipe = recipeMap.findRecipe(Long.MAX_VALUE, recipe.getInputs().stream().map(cir -> {
                    ItemStack[] stacks = cir.getIngredient().getMatchingStacks();
                    if (stacks.length > 0) {
                        stacks[0].setCount(Math.max(1, cir.getCount()));
                        return stacks[0];
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList()), recipe.getFluidInputs(), Integer.MAX_VALUE, MatchingMode.DEFAULT);
                /*GTLog.logger.info("[Recipe Checker] Found: " + foundRecipe + " from " + recipe);
                GTLog.logger.info("[Recipe Checker] Matched: " + (foundRecipe == recipe));*/
                if (foundRecipe != recipe) {
                    mismatchedRecipes.add(Pair.of(recipe, foundRecipe));
                }
            }
        }

        GTLog.logger.info("[Recipe Checker] Completed recipe check!");
        GTLog.logger.info("[Recipe Checker] Found " + mismatchedRecipes.size() + " potential conflicts");
        for (Pair<Recipe, Recipe> mismatch : mismatchedRecipes) {
            GTLog.logger.error("Tried: " + mismatch.getLeft());
            GTLog.logger.error("Found: " + mismatch.getRight());
        }
    }
}
