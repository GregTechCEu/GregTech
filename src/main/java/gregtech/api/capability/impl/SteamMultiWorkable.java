package gregtech.api.capability.impl;

import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import net.minecraft.item.ItemStack;


/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    public SteamMultiWorkable(RecipeMapSteamMultiblockController tileEntity, double conversionRate) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
    }

    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.APPEND_ITEMS;
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        RecipeBuilder<?> newRecipe = getRecipeMap().recipeBuilder();
        if(!recipe.getOutputs().isEmpty()) {
            ItemStack firstOutput = recipe.getOutputs().get(0).copy();
            newRecipe.append(recipe, 1, false);
            newRecipe.clearOutputs();
            newRecipe.outputs(firstOutput);
        }
        else if(!recipe.getChancedOutputs().isEmpty()) {
            Recipe.ChanceEntry chanced = recipe.getChancedOutputs().get(0).copy();
            newRecipe.append(recipe, 1, false);
            newRecipe.clearChancedOutput();
            newRecipe.chancedOutput(chanced.getItemStack(), chanced.getChance(), chanced.getBoostPerTier());
        }

        if(newRecipe == null) {
            return false;
        }

        return super.prepareRecipe(newRecipe.build().getResult());
    }

    @Override
    public void applyParallelBonus(RecipeBuilder<?> builder) {
        int currentRecipeEU = builder.getEUt();
        int currentRecipeDuration = builder.getDuration() / getParallelLimit();
        builder.EUt((int) Math.min(32.0, Math.ceil(currentRecipeEU) * 1.33))
           .duration((int) (currentRecipeDuration * 1.5));
    }
}
