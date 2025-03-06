package gregtech.api.recipes.logic.statemachine.running;

import net.minecraft.nbt.NBTTagCompound;

public class RecipeCleanupSaveOperation extends RecipeCleanupOperation {

    public static final String STANDARD_PREVIOUS_RECIPE_KEY = "PreviousRecipe";
    public static final RecipeCleanupSaveOperation STANDARD_INSTANCE = new RecipeCleanupSaveOperation();

    protected final String keySave;

    protected RecipeCleanupSaveOperation() {
        super();
        this.keySave = STANDARD_PREVIOUS_RECIPE_KEY;
    }

    public RecipeCleanupSaveOperation(String keyRecipe, String keyProgress, String keyBonus, String keySave) {
        super(keyRecipe, keyProgress, keyBonus);
        this.keySave = keySave;
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
        data.setTag(keySave, recipe);
    }
}
