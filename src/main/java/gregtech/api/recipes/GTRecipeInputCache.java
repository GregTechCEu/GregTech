package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.persistence.PersistentData;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cache of GTRecipeInput instances for deduplication.
 * <p>
 * Each GTRecipeInput is cached by an internal hashtable, and any duplicative instances will be replaced by identical
 * object previously created.
 * <p>
 * Caching and duplication is only available during recipe registration; once recipe registration is over, the cache
 * will be discarded and no further entries will be put into cache.
 */
public final class GTRecipeInputCache {

    private static final int MINIMUM_CACHE_SIZE = 1 << 13;
    private static final int MAXIMUM_CACHE_SIZE = 1 << 30;

    private static ObjectOpenHashSet<GTRecipeInput> instances;

    private static final String DATA_NAME = "expectedIngredientInstances";

    private GTRecipeInputCache() {}

    public static boolean isCacheEnabled() {
        return instances != null;
    }

    @ApiStatus.Internal
    public static void enableCache() {
        if (!isCacheEnabled()) {
            int size = calculateOptimalExpectedSize();
            instances = new ObjectOpenHashSet<>(size);

            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                GTLog.logger.info("GTRecipeInput cache enabled with expected size {}", size);
            }
        }
    }

    @ApiStatus.Internal
    public static void disableCache() {
        if (isCacheEnabled()) {
            int size = instances.size();
            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                GTLog.logger.info("GTRecipeInput cache disabled; releasing {} unique instances", size);
            }
            instances = null;

            if (size >= MINIMUM_CACHE_SIZE && size < MAXIMUM_CACHE_SIZE) {
                NBTTagCompound tagCompound = PersistentData.instance().getTag();
                if (getExpectedInstanceAmount(tagCompound) != size) {
                    tagCompound.setInteger(DATA_NAME, size);
                    PersistentData.instance().save();
                }
            }
        }
    }

    private static int getExpectedInstanceAmount(@NotNull NBTTagCompound tagCompound) {
        return MathHelper.clamp(tagCompound.getInteger(DATA_NAME), MINIMUM_CACHE_SIZE, MAXIMUM_CACHE_SIZE);
    }

    /**
     * Tries to deduplicate the instance with previously cached instances. If there is no identical GTRecipeInput
     * present in cache, the {@code recipeInput} will be put into cache, marked as cached, and returned subsequently.
     * <p>
     * This operation returns {@code recipeInput} without doing anything if cache is disabled.
     *
     * @param recipeInput ingredient instance to be deduplicated
     * @return Either previously cached instance, or {@code recipeInput} marked cached; or unmodified
     *         {@code recipeInput} instance if the cache is disabled
     */
    public static GTRecipeInput deduplicate(GTRecipeInput recipeInput) {
        if (!isCacheEnabled() || recipeInput.isCached()) {
            return recipeInput;
        }
        GTRecipeInput cached = instances.addOrGet(recipeInput);
        if (cached == recipeInput) { // If recipeInput is cached just now...
            cached.setCached();
        }
        return cached;
    }

    /**
     * Tries to deduplicate each instance in the list with previously cached instances. If there is no identical
     * GTRecipeInput present in cache, the {@code recipeInput} will be put into cache, marked as cached, and returned
     * subsequently.
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

    /**
     * Calculates the optimal expected size for the input cache:
     * <ol>
     * <li>Pick a Load Factor to test: i.e. {@code 0.75f} (default).</li>
     * <li>Pick a Size to test: i.e. {@code 8192}.</li>
     * <li>Internal array's size: next highest power of 2 for {@code size / loadFactor},
     * {@code nextHighestPowerOf2(8192 / 0.75) = 16384}.</li>
     * <li>The maximum amount of stored values before a rehash is required {@code arraySize * loadFactor},
     * {@code 16384 * 0.75 = 12288}.</li>
     * <li>Compare with the known amount of values stored: {@code 12288 >= 11774}.</li>
     * <li>If larger or equal, the initial capacity and load factor will not induce a rehash/resize.</li>
     * </ol>
     *
     * @return the optimal expected input cache size
     */
    private static int calculateOptimalExpectedSize() {
        int min = Math.max(getExpectedInstanceAmount(PersistentData.instance().getTag()), MINIMUM_CACHE_SIZE);
        for (int i = 13; i < 31; i++) {
            int sizeToTest = 1 << i;
            int arraySize = nextHighestPowerOf2((int) (sizeToTest / Hash.DEFAULT_LOAD_FACTOR));
            int maxStoredBeforeRehash = (int) (arraySize * Hash.DEFAULT_LOAD_FACTOR);

            if (maxStoredBeforeRehash >= min) {
                return sizeToTest;
            }
        }
        return MINIMUM_CACHE_SIZE;
    }

    /**
     * <a href="https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2">Algorithm source.</a>
     *
     * @param x the number to use
     * @return the next highest power of 2 relative to the number
     */
    private static int nextHighestPowerOf2(int x) {
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        x++;
        return x;
    }
}
