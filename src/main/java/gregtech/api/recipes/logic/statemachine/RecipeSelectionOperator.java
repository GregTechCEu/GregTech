package gregtech.api.recipes.logic.statemachine;

import gregtech.api.recipes.Recipe;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeSelectionOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_RECIPE_KEY = "SelectedRecipe";
    public static final String STANDARD_SUCCESS_KEY = "RecipeSelected";

    public static final Predicate<NBTTagCompound> SUCCESS_PREDICATE = t -> t.getBoolean(STANDARD_SUCCESS_KEY);

    protected final @Nullable Predicate<Recipe> predicate;
    protected final String keyIterator;
    protected final String keyResult;
    protected final String keySuccess;

    public RecipeSelectionOperator(@Nullable Predicate<Recipe> predicate) {
        this.predicate = predicate;
        keyIterator = RecipeSearchOperator.STANDARD_RESULT_KEY;
        keyResult = STANDARD_RECIPE_KEY;
        keySuccess = STANDARD_SUCCESS_KEY;
    }

    public RecipeSelectionOperator(@Nullable Predicate<Recipe> predicate, String keyIterator, String keyResult,
                                   String keySuccess) {
        this.predicate = predicate;
        this.keyIterator = keyIterator;
        this.keyResult = keyResult;
        this.keySuccess = keySuccess;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        Iterator<Recipe> iter = (Iterator<Recipe>) transientData.get(keyIterator);

        if (iter == null) throw new IllegalStateException();

        data.setBoolean(keySuccess, false);
        while (iter.hasNext()) {
            Recipe next = iter.next();
            if (predicate == null || predicate.test(next)) {
                transientData.put(keyResult, next);
                data.setBoolean(keySuccess, true);
                return;
            }
        }
    }
}
