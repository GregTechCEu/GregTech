package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.nbt.NBTTagCompound;

public class RecipeDegressOperation implements GTStateMachineOperator {

    public static final RecipeDegressOperation STANDARD_INSTANCE = new RecipeDegressOperation();

    protected final String keyRecipes;
    protected final String keyProgress;
    protected final String keyBonus;

    protected RecipeDegressOperation() {
        this.keyRecipes = RecipeFinalizer.STANDARD_RECIPES_KEY;
        this.keyProgress = RecipeProgressOperation.STANDARD_PROGRESS_KEY;
        this.keyBonus = RecipeProgressOperation.STANDARD_BONUS_PROGRESS_KEY;
    }

    public RecipeDegressOperation(String keyRecipes, String keyProgress, String keyBonus) {
        this.keyRecipes = keyRecipes;
        this.keyProgress = keyProgress;
        this.keyBonus = keyBonus;
    }

    @Override
    public void operate(NBTTagCompound data) {
        NBTTagCompound recipe = data.getCompoundTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY);
        int progress = recipe.getInteger(keyProgress);
        if (progress > 0) {
            recipe.setInteger(keyProgress, Math.max(0, progress - 2));
        }
    }
}
