package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.properties.impl.ComputationProperty;
import gregtech.api.recipes.properties.impl.TotalComputationProperty;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class RecipeComputationFinalizer extends RecipeFinalizer {

    public static final RecipeComputationFinalizer STANDARD_INSTANCE = new RecipeComputationFinalizer();

    public @NotNull NBTTagCompound finalize(@NotNull RecipeRun run) {
        NBTTagCompound tag = super.finalize(run);

        int total = run.getRecipeView().getRecipe().getProperty(TotalComputationProperty.getInstance(), -1);
        if (total > 0) tag.setInteger("TotalCWU", total);
        int perT = run.getRecipeView().getRecipe().getProperty(ComputationProperty.getInstance(), -1);
        if (perT > 0) tag.setInteger("CWUt", perT);

        return tag;
    }

    public static int getTotalCWU(NBTTagCompound recipe) {
        return recipe.getInteger("TotalCWU");
    }

    public static int getCWUt(NBTTagCompound recipe) {
        return recipe.getInteger("CWUt");
    }
}
