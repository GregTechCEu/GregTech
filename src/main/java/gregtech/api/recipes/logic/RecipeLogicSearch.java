package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;

import gregtech.api.util.GuardedData;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class RecipeLogicSearch {

    public static final MapKey<List<ItemStack>> SEARCH_ITEMS_KEY = new MapKey<>();
    public static final MapKey<List<FluidStack>> SEARCH_FLUIDS_KEY = new MapKey<>();
    public static final MapKey.LongKey SEARCH_VOLTAGE_KEY = new MapKey.LongKey();
    public static final MapKey<RecipeMap<?>> SEARCH_MAP_KEY = new MapKey<>();
    public static final MapKey<Iterator<Recipe>> SEARCH_RESULTS_KEY = new MapKey<>();

    public static void loadSearchItems(GuardedData<Map<MapKey<?>, Object>> data, List<ItemStack> items) {
        SEARCH_ITEMS_KEY.put(data.getTransientData(), items);
    }

    public static void loadSearchFluids(GuardedData<Map<MapKey<?>, Object>> data, List<FluidStack> fluids) {
        SEARCH_FLUIDS_KEY.put(data.getTransientData(), fluids);
    }

    public static void loadSearchVoltage(GuardedData<Map<MapKey<?>, Object>> data, long voltage) {
        SEARCH_VOLTAGE_KEY.putLong(data.getTransientData(), voltage);
    }

    public static void loadSearchMap(GuardedData<Map<MapKey<?>, Object>> data, RecipeMap<?> map) {
        SEARCH_MAP_KEY.put(data.getTransientData(), map);
    }

    public static void search(GuardedData<Map<MapKey<?>, Object>> data) {
        var tData = data.getTransientData();
        RecipeMap<?> map = SEARCH_MAP_KEY.get(tData);
        if (map != null) {
            Recipe recipe = map.findRecipe(SEARCH_VOLTAGE_KEY.getLong(tData, 1),
                    SEARCH_ITEMS_KEY.getNonnull(tData, Collections.emptyList()),
                    SEARCH_FLUIDS_KEY.getNonnull(tData, Collections.emptyList()));
            if (recipe != null) {
                SEARCH_RESULTS_KEY.put(tData, Collections.singleton(recipe).iterator());
            } else {
                SEARCH_RESULTS_KEY.put(tData, Collections.emptyIterator());
            }
        }
    }

}
