package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class RecipeCleanupOperation implements GTStateMachineOperator {

    public static final String STANDARD_RECIPE_KEY = "ProcessingRecipe";
    public static final RecipeCleanupOperation STANDARD_INSTANCE = new RecipeCleanupOperation();

    protected final String keyRecipe;
    protected final String keyProgress;
    protected final String keyBonus;

    protected RecipeCleanupOperation() {
        this.keyRecipe = STANDARD_RECIPE_KEY;
        this.keyProgress = RecipeProgressOperation.STANDARD_PROGRESS_KEY;
        this.keyBonus = RecipeProgressOperation.STANDARD_BONUS_PROGRESS_KEY;
    }

    public RecipeCleanupOperation(String keyRecipe, String keyProgress, String keyBonus) {
        this.keyRecipe = keyRecipe;
        this.keyProgress = keyProgress;
        this.keyBonus = keyBonus;
    }

    @Override
    public void operate(NBTTagCompound data) {
        NBTTagCompound recipe = data.getCompoundTag(keyRecipe);
        int progress = recipe.getInteger(keyProgress);
        double duration = recipe.getDouble("Duration");
        if (duration > progress) throw new IllegalStateException();

        double bonus = progress - duration;
        if (bonus > 0) {
            data.setDouble(keyBonus, data.getDouble(keyBonus) + bonus);
        }
    }

    @Contract(pure = true)
    public static @NotNull Predicate<NBTTagCompound> recipeIsComplete() {
        return recipeIsComplete(STANDARD_RECIPE_KEY, RecipeProgressOperation.STANDARD_PROGRESS_KEY);
    }

    @Contract(pure = true)
    public static @NotNull Predicate<NBTTagCompound> recipeIsComplete(String keyRecipe, String keyProgress) {
        return t -> t.getCompoundTag(keyRecipe).getInteger(keyProgress) >
                t.getCompoundTag(keyRecipe).getDouble("Duration");
    }
}
