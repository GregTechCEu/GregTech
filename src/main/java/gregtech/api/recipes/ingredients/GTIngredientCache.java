package gregtech.api.recipes.ingredients;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class GTIngredientCache {
    public static final WeakHashMap<GTRecipeOreInput, WeakReference<GTRecipeOreInput>> ORE_INSTANCES = new WeakHashMap<>();
    public static final WeakHashMap<GTRecipeItemInput, WeakReference<GTRecipeItemInput>> INSTANCES = new WeakHashMap<>();
    public static final WeakHashMap<GTRecipeOreInput, WeakReference<GTRecipeOreInput>> NON_CONSUMABLE_ORE_INSTANCES = new WeakHashMap<>();
    public static final WeakHashMap<GTRecipeItemInput, WeakReference<GTRecipeItemInput>> NON_CONSUMABLE_INSTANCES = new WeakHashMap<>();
    public static final WeakHashMap<GTRecipeFluidInput, WeakReference<GTRecipeFluidInput>> FLUID_INSTANCES = new WeakHashMap<>();
    public static final WeakHashMap<GTRecipeFluidInput, WeakReference<GTRecipeFluidInput>> NON_CONSUMABLE_FLUID_INSTANCES = new WeakHashMap<>();
}
