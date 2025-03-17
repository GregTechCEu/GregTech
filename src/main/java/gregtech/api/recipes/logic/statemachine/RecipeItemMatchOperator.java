package gregtech.api.recipes.logic.statemachine;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.statemachine.parallel.RecipeParallelLimitOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeItemMatchOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_RESULT_KEY = "ItemMatchCalculation";
    public static final String STANDARD_MAXCOUNT_KEY = "MaxItemParallel";
    public static final Predicate<NBTTagCompound> SUCCESS_PREDICATE = t -> t.getInteger(STANDARD_MAXCOUNT_KEY) > 0;
    public static final RecipeItemMatchOperator STANDARD_INSTANCE = new RecipeItemMatchOperator();

    protected final String keyItems;
    protected final String keyRecipe;
    protected final String keyResult;
    protected final String keyLimit;
    protected final String keyMaxOut;

    protected RecipeItemMatchOperator() {
        this.keyItems = RecipeSearchOperator.STANDARD_ITEMS_KEY;
        this.keyRecipe = RecipeSelectionOperator.STANDARD_RECIPE_KEY;
        this.keyResult = STANDARD_RESULT_KEY;
        this.keyLimit = RecipeParallelLimitOperator.STANDARD_LIMIT_KEY;
        this.keyMaxOut = STANDARD_MAXCOUNT_KEY;
    }

    public RecipeItemMatchOperator(String keyItems, String keyRecipe, String keyResult, String keyLimit,
                                   String keyMaxOut) {
        this.keyItems = keyItems;
        this.keyRecipe = keyRecipe;
        this.keyResult = keyResult;
        this.keyLimit = keyLimit;
        this.keyMaxOut = keyMaxOut;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        List<ItemStack> items = (List<ItemStack>) transientData.get(keyItems);
        Recipe recipe = (Recipe) transientData.get(keyRecipe);
        int limit = data.hasKey(keyLimit) ? data.getInteger(keyLimit) : 1;
        if (items == null) {
            data.setInteger(keyMaxOut, limit);
            return;
        }
        if (recipe != null && limit > 0) {
            MatchCalculation<ItemStack> match = IngredientMatchHelper.matchItems(recipe.getItemIngredients(), items);
            transientData.put(keyResult, match);
            data.setInteger(keyMaxOut, match.largestSucceedingScale(limit));
            return;
        }
        data.setInteger(keyMaxOut, 0);
    }
}
