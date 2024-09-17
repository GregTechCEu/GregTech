package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;

import org.jetbrains.annotations.Nullable;

public interface RecipeRunner {

    @Nullable
    Recipe getPrevious();

    @Nullable
    RecipeRun getCurrent();

    void setRunning(@Nullable Recipe recipe, @Nullable RecipeRun run);

    void notifyOfCompletion();

    double getRecipeProgress();

    void setRecipeProgress(double progress);

    void setOutputInvalid(boolean invalid);

    boolean isOutputInvalid();
}
