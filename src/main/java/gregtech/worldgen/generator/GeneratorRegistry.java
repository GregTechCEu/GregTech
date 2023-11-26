package gregtech.worldgen.generator;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;

public final class GeneratorRegistry<T extends GeneratorSettings> {

    private final Int2ObjectMap<List<T>> map = new Int2ObjectOpenHashMap<>();
    private final Int2IntOpenHashMap weightMap = new Int2IntOpenHashMap();

    /**
     * @param generator the generator to register
     */
    public void register(@NotNull T generator) {
        int[] dimensions = generator.allowedDimensions();
        for (int dimension : dimensions) {
            List<T> list = map.get(dimension);
            if (list == null) {
                list = new ArrayList<>();
                map.put(dimension, list);
            }
            list.add(generator);
            weightMap.put(dimension, weightMap.get(dimension) + generator.weight());
        }
    }

    /**
     * @param dimension the dimension to retrieve for
     * @return the total weight of all objects in this registry for a dimension
     */
    public int getTotalWeight(int dimension) {
        return weightMap.get(dimension);
    }

    /**
     * @param dimension the dimension to retrieve for
     * @return all the generators for the dimension
     */
    public @Nullable @UnmodifiableView List<@NotNull T> getGenerators(int dimension) {
        return map.get(dimension);
    }

    /**
     * @param dimension the dimension to check
     * @return if there are any generators associated with the dimension
     */
    public boolean hasGenerators(int dimension) {
        return map.containsKey(dimension);
    }

    /**
     * @return all the dimensions registered
     */
    public @NotNull @UnmodifiableView IntCollection getDimensions() {
        return map.keySet();
    }
}
