package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.nbt.NBTTagCompound;

public class RecipeProgressOperation implements GTStateMachineOperator {

    public static final String STANDARD_PROGRESS_KEY = "Progress";
    public static final String STANDARD_BONUS_PROGRESS_KEY = "RecipeBonusProgress";
    public static final RecipeProgressOperation STANDARD_INSTANCE = new RecipeProgressOperation();

    protected final String keyRecipe;
    protected final String keyProgress;
    protected final String keyBonus;

    protected RecipeProgressOperation() {
        this.keyRecipe = RecipeCleanupOperation.STANDARD_RECIPE_KEY;
        this.keyProgress = STANDARD_PROGRESS_KEY;
        this.keyBonus = STANDARD_BONUS_PROGRESS_KEY;
    }

    public RecipeProgressOperation(String keyRecipe, String keyProgress, String keyBonus) {
        this.keyRecipe = keyRecipe;
        this.keyProgress = keyProgress;
        this.keyBonus = keyBonus;
    }

    @Override
    public void operate(NBTTagCompound data) {
        NBTTagCompound recipe = data.getCompoundTag(keyRecipe);
        double bonus = data.getDouble(keyBonus);
        int progress = 1;
        if (bonus > 1) {
            progress += (int) bonus;
            if (bonus + 1 - progress == 0) {
                data.removeTag(keyBonus);
            } else {
                data.setDouble(keyBonus, bonus + 1 - progress);
            }
        }
        recipe.setInteger(keyProgress, recipe.getInteger(keyProgress) + progress);
    }
}
