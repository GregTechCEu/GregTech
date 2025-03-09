package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.nbt.NBTTagCompound;

public class RecipeResetOperation implements GTStateMachineOperator {

    public static final RecipeResetOperation STANDARD_INSTANCE = new RecipeResetOperation();

    protected final String keyRecipe;
    protected final String keyProgress;
    protected final String keyBonus;

    protected RecipeResetOperation() {
        this.keyRecipe = RecipeCleanupOperation.STANDARD_RECIPE_KEY;
        this.keyProgress = RecipeProgressOperation.STANDARD_PROGRESS_KEY;
        this.keyBonus = RecipeProgressOperation.STANDARD_BONUS_PROGRESS_KEY;
    }

    public RecipeResetOperation(String keyRecipe, String keyProgress, String keyBonus) {
        this.keyRecipe = keyRecipe;
        this.keyProgress = keyProgress;
        this.keyBonus = keyBonus;
    }

    @Override
    public void operate(NBTTagCompound data) {
        NBTTagCompound recipe = data.getCompoundTag(keyRecipe);
        int progress = recipe.getInteger(keyProgress);
        if (progress > 0) {
            recipe.setInteger(keyProgress, 0);
        }
    }
}
