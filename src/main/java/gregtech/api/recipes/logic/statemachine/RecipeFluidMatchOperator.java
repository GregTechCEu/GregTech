package gregtech.api.recipes.logic.statemachine;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.statemachine.parallel.RecipeParallelLimitOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RecipeFluidMatchOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_RESULT_KEY = "FluidMatchCalculation";
    public static final String STANDARD_MAXCOUNT_KEY = "MaxFluidParallel";
    public static final Predicate<NBTTagCompound> SUCCESS_PREDICATE = t -> t.getInteger(STANDARD_MAXCOUNT_KEY) > 0;
    public static final RecipeFluidMatchOperator STANDARD_INSTANCE = new RecipeFluidMatchOperator();

    protected final String keyFluids;
    protected final String keyRecipe;
    protected final String keyResult;
    protected final String keyLimit;
    protected final String keyMaxOut;

    protected RecipeFluidMatchOperator() {
        this.keyFluids = RecipeSearchOperator.STANDARD_FLUIDS_KEY;
        this.keyRecipe = RecipeSelectionOperator.STANDARD_RECIPE_KEY;
        this.keyResult = STANDARD_RESULT_KEY;
        this.keyLimit = RecipeParallelLimitOperator.STANDARD_LIMIT_KEY;
        this.keyMaxOut = STANDARD_MAXCOUNT_KEY;
    }

    public RecipeFluidMatchOperator(String keyFluids, String keyRecipe, String keyResult, String keyLimit,
                                    String keyMaxOut) {
        this.keyFluids = keyFluids;
        this.keyRecipe = keyRecipe;
        this.keyResult = keyResult;
        this.keyLimit = keyLimit;
        this.keyMaxOut = keyMaxOut;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        List<FluidStack> fluids = (List<FluidStack>) transientData.get(keyFluids);
        Recipe recipe = (Recipe) transientData.get(keyRecipe);
        int limit = data.hasKey(keyLimit) ? data.getInteger(keyLimit) : 1;
        if (fluids == null) {
            data.setInteger(keyMaxOut, limit);
            return;
        }
        if (recipe != null && limit > 0) {
            MatchCalculation<FluidStack> match = IngredientMatchHelper.matchFluids(recipe.getFluidIngredients(),
                    fluids);
            transientData.put(keyResult, match);
            data.setInteger(keyMaxOut, match.largestSucceedingScale(limit));
            return;
        }
        data.setInteger(keyMaxOut, 0);
    }
}
