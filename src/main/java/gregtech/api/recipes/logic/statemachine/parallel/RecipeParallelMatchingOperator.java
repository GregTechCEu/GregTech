package gregtech.api.recipes.logic.statemachine.parallel;

import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.StandardRecipeView;
import gregtech.api.recipes.logic.statemachine.RecipeFluidMatchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeItemMatchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeViewOperator;
import gregtech.api.statemachine.GTStateMachineOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public class RecipeParallelMatchingOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_ITEM_AMOUNT_LIMIT_KEY = "ItemAmountLimit";
    public static final String STANDARD_ITEM_UNIQUE_LIMIT_KEY = "ItemUniqueLimit";
    public static final String STANDARD_FLUID_AMOUNT_LIMIT_KEY = "FluidAmountLimit";
    public static final String STANDARD_FLUID_UNIQUE_LIMIT_KEY = "FluidUniqueLimit";
    public static final RecipeParallelMatchingOperator STANDARD_INSTANCE = new RecipeParallelMatchingOperator();

    protected final String keyRecipeView;
    protected final String keyItemMatch;
    protected final String keyFluidMatch;
    protected final String keyLimit;

    protected final String keyItemAmountLimit;
    protected final String keyItemUniqueLimit;
    protected final String keyFluidAmountLimit;
    protected final String keyFluidUniqueLimit;

    protected RecipeParallelMatchingOperator() {
        keyRecipeView = RecipeViewOperator.STANDARD_VIEW_KEY;
        keyItemMatch = RecipeItemMatchOperator.STANDARD_RESULT_KEY;
        keyFluidMatch = RecipeFluidMatchOperator.STANDARD_RESULT_KEY;
        keyLimit = RecipeParallelLimitOperator.STANDARD_LIMIT_KEY;
        keyItemAmountLimit = STANDARD_ITEM_AMOUNT_LIMIT_KEY;
        keyItemUniqueLimit = STANDARD_ITEM_UNIQUE_LIMIT_KEY;
        keyFluidAmountLimit = STANDARD_FLUID_AMOUNT_LIMIT_KEY;
        keyFluidUniqueLimit = STANDARD_FLUID_UNIQUE_LIMIT_KEY;
    }

    public RecipeParallelMatchingOperator(String keyRecipeView, String keyItemMatch, String keyFluidMatch,
                                          String keyLimit,
                                          String keyItemAmountLimit, String keyItemUniqueLimit,
                                          String keyFluidAmountLimit, String keyFluidUniqueLimit) {
        this.keyRecipeView = keyRecipeView;
        this.keyItemMatch = keyItemMatch;
        this.keyFluidMatch = keyFluidMatch;
        this.keyLimit = keyLimit;
        this.keyItemAmountLimit = keyItemAmountLimit;
        this.keyItemUniqueLimit = keyItemUniqueLimit;
        this.keyFluidAmountLimit = keyFluidAmountLimit;
        this.keyFluidUniqueLimit = keyFluidUniqueLimit;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        StandardRecipeView recipe = (StandardRecipeView) transientData.get(keyRecipeView);
        MatchCalculation<ItemStack> itemMatch = (MatchCalculation<ItemStack>) transientData.get(keyItemMatch);
        MatchCalculation<FluidStack> fluidMatch = (MatchCalculation<FluidStack>) transientData.get(keyFluidMatch);
        if (recipe == null) throw new IllegalStateException();

        int parallel = data.hasKey(keyLimit) ? data.getInteger(keyLimit) : 1;
        int itemAmount = data.hasKey(keyItemAmountLimit) ? data.getInteger(keyItemAmountLimit) : Integer.MAX_VALUE;
        int itemUnique = data.hasKey(keyItemUniqueLimit) ? data.getInteger(keyItemUniqueLimit) : Integer.MAX_VALUE;
        int fluidAmount = data.hasKey(keyFluidAmountLimit) ? data.getInteger(keyFluidAmountLimit) : Integer.MAX_VALUE;
        int fluidUnique = data.hasKey(keyFluidUniqueLimit) ? data.getInteger(keyFluidUniqueLimit) : Integer.MAX_VALUE;

        if (parallel > 0 && itemMatch != null) parallel = itemMatch.largestSucceedingScale(parallel);
        if (parallel > 0 && fluidMatch != null) parallel = fluidMatch.largestSucceedingScale(parallel);
        int minValue = 0;
        while (parallel - minValue > 1) {
            int middle = (minValue + parallel) / 2;
            if (fits(recipe.setParallel(middle), itemAmount, itemUnique, fluidAmount, fluidUnique)) {
                minValue = middle;
            } else {
                parallel = middle;
            }
        }
        if (!fits(recipe.setParallel(parallel), itemAmount, itemUnique, fluidAmount, fluidUnique)) {
            recipe.setParallel(minValue);
        }
    }

    protected boolean fits(StandardRecipeView view, int itemAmount, int itemUnique, int fluidAmount, int fluidUnique) {
        List<ItemStack> outI = view.getMaximumItems();
        List<FluidStack> outF = view.getMaximumFluids();
        if (outI.size() > itemUnique || outF.size() > fluidUnique) return false;
        int sum = 0;
        for (ItemStack stack : outI) {
            sum += stack.getCount();
            if (sum > itemAmount) return false;
        }
        sum = 0;
        for (FluidStack stack : outF) {
            sum += stack.amount;
            if (sum > fluidAmount) return false;
        }
        return true;
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator itemAmountLimitProvider(IntSupplier limit) {
        return limitProvider(limit, STANDARD_ITEM_AMOUNT_LIMIT_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator itemUniqueLimitProvider(IntSupplier limit) {
        return limitProvider(limit, STANDARD_ITEM_UNIQUE_LIMIT_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator fluidAmountLimitProvider(IntSupplier limit) {
        return limitProvider(limit, STANDARD_FLUID_AMOUNT_LIMIT_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator fluidUniqueLimitProvider(IntSupplier limit) {
        return limitProvider(limit, STANDARD_FLUID_UNIQUE_LIMIT_KEY);
    }

    @Contract(pure = true)
    public static @NotNull GTStateMachineOperator limitProvider(IntSupplier limit, String key) {
        return t -> t.setInteger(key, limit.getAsInt());
    }
}
