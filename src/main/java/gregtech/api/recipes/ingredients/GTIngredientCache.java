package gregtech.api.recipes.ingredients;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class GTIngredientCache {
    public static WeakHashMap<GTRecipeOreInput, WeakReference<GTRecipeOreInput>> ORE_INSTANCES = new WeakHashMap<>();
    public static WeakHashMap<GTRecipeItemInput, WeakReference<GTRecipeItemInput>> INSTANCES = new WeakHashMap<>();
    public static WeakHashMap<GTRecipeItemInput, WeakReference<GTRecipeItemInput>> NON_CONSUMABLE_INSTANCES = new WeakHashMap<>();
    public static WeakHashMap<GTRecipeFluidInput, WeakReference<GTRecipeFluidInput>> FLUID_INSTANCES = new WeakHashMap<GTRecipeFluidInput, WeakReference<GTRecipeFluidInput>>();

}
