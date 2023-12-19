package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cache of GTRecipeInput instances for deduplication.
 * <p>
 * Each GTRecipeInput is cached by an internal hashtable, and any duplicative
 * instances will be replaced by identical object previously created.
 * <p>
 * Caching and duplication is only available during recipe registration; once
 * recipe registration is over, the cache will be discarded and no further entries
 * will be put into cache.
 */
public class GTRecipeInputCache {

    private static final int EXPECTED_CACHE_SIZE = 16384;
    private static ObjectOpenHashSet<GTRecipeInput> INSTANCES;

    public static boolean isCacheEnabled() {
        return INSTANCES != null;
    }

    public static void enableCache() {
        if (!isCacheEnabled()) {
            INSTANCES = new ObjectOpenHashSet<>(EXPECTED_CACHE_SIZE, 1);
            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment())
                GTLog.logger.info("GTRecipeInput cache enabled");
        }
    }

    public static void disableCache() {
        if (isCacheEnabled()) {
            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment())
                GTLog.logger.info("GTRecipeInput cache disabled; releasing {} unique instances", INSTANCES.size());
            INSTANCES = null;
        }
    }

    /**
     * Tries to deduplicate the instance with previously cached instances.
     * If there is no identical GTRecipeInput present in cache, the
     * {@code recipeInput} will be put into cache, marked as cached, and returned subsequently.
     * <p>
     * This operation returns {@code recipeInput} without doing anything if cache is disabled.
     *
     * @param recipeInput ingredient instance to be deduplicated
     * @return Either previously cached instance, or {@code recipeInput} marked cached;
     *         or unmodified {@code recipeInput} instance if the cache is disabled
     */
    public static GTRecipeInput deduplicate(GTRecipeInput recipeInput) {
        if (!isCacheEnabled() || recipeInput.isCached()) {
            return recipeInput;
        }
        GTRecipeInput cached = INSTANCES.addOrGet(recipeInput);
        if (cached == recipeInput) { // If recipeInput is cached just now...
            cached.setCached();
        }
        return cached;
    }

    /**
     * Tries to deduplicate each instance in the list with previously cached instances.
     * If there is no identical GTRecipeInput present in cache, the
     * {@code recipeInput} will be put into cache, marked as cached, and returned subsequently.
     * <p>
     * This operation returns {@code inputs} without doing anything if cache is disabled.
     *
     * @param inputs list of ingredient instances to be deduplicated
     * @return List of deduplicated instances, or {@code inputs} if the cache is disabled
     */
    public static List<GTRecipeInput> deduplicateInputs(List<GTRecipeInput> inputs) {
        if (!isCacheEnabled()) {
            return inputs;
        }
        if (inputs.isEmpty()) {
            return Collections.emptyList();
        }
        List<GTRecipeInput> list = new ArrayList<>(inputs.size());
        for (GTRecipeInput input : inputs) {
            list.add(deduplicate(input));
        }
        return list;
    }
}
