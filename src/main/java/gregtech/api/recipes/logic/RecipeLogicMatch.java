package gregtech.api.recipes.logic;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.util.GuardedData;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static gregtech.api.recipes.logic.RecipeLogicSearch.SEARCH_RESULTS_KEY;

public abstract class RecipeLogicMatch {

    public static final MapKey<List<ItemStack>> MATCH_ITEMS_KEY = new MapKey<>();
    public static final MapKey<List<FluidStack>> MATCH_FLUIDS_KEY = new MapKey<>();
    public static final MapKey.LongKey MATCH_VOLTAGE_KEY = new MapKey.LongKey();
    public static final MapKey.IntKey PARALLEL_LIMIT_KEY = new MapKey.IntKey();
    public static final MapKey.IntKey PARALLEL_KEY = new MapKey.IntKey();
    public static final MapKey<Recipe> SELECTED_RECIPE_KEY = new MapKey<>();

    public static final MapKey<MatchCalculation<ItemStack>> ITEM_MATCH_KEY = new MapKey<>();
    public static final MapKey<MatchCalculation<FluidStack>> FLUID_MATCH_KEY = new MapKey<>();
    public static final MapKey<TrimData> TRIM_KEY = new MapKey<>();
    public static final MapKey.DoubleKey VOLTAGE_DISCOUNT_KEY = new MapKey.DoubleKey();
    public static final MapKey<RecipeView> RECIPE_VIEW_KEY = new MapKey<>();

    //----------//
    // matching //
    //----------//

    public static void loadMatchItems(GuardedData<Map<MapKey<?>, Object>> data, List<ItemStack> items) {
        MATCH_ITEMS_KEY.put(data.getTransientData(), items);
    }

    public static void loadMatchFluids(GuardedData<Map<MapKey<?>, Object>> data, List<FluidStack> fluids) {
        MATCH_FLUIDS_KEY.put(data.getTransientData(), fluids);
    }

    public static void loadMatchVoltage(GuardedData<Map<MapKey<?>, Object>> data, long voltage) {
        MATCH_VOLTAGE_KEY.putLong(data.getTransientData(), voltage);
    }

    public static void loadParallelLimit(GuardedData<Map<MapKey<?>, Object>> data, int parallelLimit) {
        PARALLEL_LIMIT_KEY.put(data.getTransientData(), parallelLimit);
    }

    public static void matchRecipe(GuardedData<Map<MapKey<?>, Object>> data, @NotNull BiPredicate<GuardedData<Map<MapKey<?>, Object>>, Recipe> predicate) {
        Iterator<Recipe> results = SEARCH_RESULTS_KEY.getNonnull(data.getTransientData(), Collections.emptyIterator());
        while (results.hasNext()) {
            Recipe next = results.next();
            if (predicate.test(data, next)) {
                int limit = (int) Math.min(PARALLEL_LIMIT_KEY.getInt(data.getTransientData(), 1), MATCH_VOLTAGE_KEY.getLong(data.getTransientData(), 1) / next.getEUt());
                if (limit <= 0) continue;
                MatchCalculation<ItemStack> itemMatch = IngredientMatchHelper.matchItems(next.getInputs(),
                        MATCH_ITEMS_KEY.getNonnull(data.getTransientData(), Collections.emptyList()));
                MatchCalculation<FluidStack> fluidMatch = IngredientMatchHelper.matchFluids(next.getFluidInputs(),
                        MATCH_FLUIDS_KEY.getNonnull(data.getTransientData(), Collections.emptyList()));
                limit = itemMatch.largestSucceedingScale(limit);
                if (limit <= 0) continue;
                limit = fluidMatch.largestSucceedingScale(limit);
                if (limit <= 0) continue;
                PARALLEL_KEY.putInt(data.getTransientData(), limit);
                ITEM_MATCH_KEY.put(data.getTransientData(), itemMatch);
                FLUID_MATCH_KEY.put(data.getTransientData(), fluidMatch);
                SELECTED_RECIPE_KEY.put(data.getTransientData(), next);
                return;
            }
        }
    }

    //-------------//
    // recipe view //
    //-------------//

    public static void loadTrimData(GuardedData<Map<MapKey<?>, Object>> data, @NotNull TrimData trim) {
        TRIM_KEY.put(data.getTransientData(), trim);
    }

    public static void loadVoltageDiscount(GuardedData<Map<MapKey<?>, Object>> data, double discount) {
        VOLTAGE_DISCOUNT_KEY.put(data.getTransientData(), discount);
    }

    public static void constructView(GuardedData<Map<MapKey<?>, Object>> data) {
        Recipe recipe = SELECTED_RECIPE_KEY.get(data.getTransientData());
        if (recipe == null) {
            RecipeLogicCore.stateError("Attempted to construct a recipe view without a recipe loaded.");
            return;
        }
        TrimData trim = TRIM_KEY.getNonnull(data.getTransientData(), TrimData.NO_TRIM);
        double discount = VOLTAGE_DISCOUNT_KEY.getDouble(data.getTransientData(), 1);
        if (recipe.getAllItemOutputs().size() < trim.itemLimit() && recipe.getAllFluidOutputs().size() < trim.fluidLimit()) {
            RECIPE_VIEW_KEY.put(data.getTransientData(), new StandardRecipeView(recipe, ITEM_MATCH_KEY.get(data.getTransientData()),
                    FLUID_MATCH_KEY.get(data.getTransientData()), discount, PARALLEL_KEY.getInt(data.getTransientData())));
        } else {
            RECIPE_VIEW_KEY.put(data.getTransientData(), new TrimmedRecipeView(recipe, ITEM_MATCH_KEY.get(data.getTransientData()),
                    FLUID_MATCH_KEY.get(data.getTransientData()), discount, PARALLEL_KEY.getInt(data.getTransientData()),
                    trim.itemLimit(), trim.fluidLimit()));
        }
    }

    //------------//
    // recipe run //
    //------------//

    public static void finalizeRecipe(GuardedData<Map<MapKey<?>, Object>> data) {
        RecipeView recipe = RECIPE_VIEW_KEY.get(data.getTransientData());
        if (recipe == null) {
            RecipeLogicCore.stateError("Attempted to finalize a recipe without a recipe view loaded.");
            return;
        }
        long voltage = RecipeLogicOverclock.OC_VOLTAGE_KEY.getLong(data.getTransientData(), recipe.getActualVoltage());
        int duration = RecipeLogicOverclock.OC_DURATION_KEY.getInt(data.getTransientData(), recipe.getActualDuration());
    }

    //------//
    // misc //
    //------//

    @Desugar
    public record TrimData(int itemLimit, int fluidLimit) {
        public static final TrimData NO_TRIM = new TrimData(Integer.MAX_VALUE, Integer.MAX_VALUE);

        public static TrimData items(int itemLimit) {
            return new TrimData(itemLimit, Integer.MAX_VALUE);
        }

        public static TrimData fluids(int fluidLimit) {
            return new TrimData(Integer.MAX_VALUE, fluidLimit);
        }
    }
}
