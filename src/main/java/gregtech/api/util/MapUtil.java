package gregtech.api.util;

import gregtech.api.graphnet.net.NetNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class MapUtil {

    public static int computeIfAbsent(@NotNull Object2IntMap<NetNode> map, @NotNull NetNode key,
                                      @NotNull ToIntFunction<NetNode> compute) {
        int val;
        if (map.containsKey(key)) {
            val = map.getInt(key);
        } else {
            val = compute.applyAsInt(key);
            map.put(key, val);
        }
        return val;
    }

    public static boolean computeIfAbsent(@NotNull Object2BooleanMap<NetNode> map, @NotNull NetNode key,
                                          @NotNull Predicate<NetNode> compute) {
        boolean val;
        if (map.containsKey(key)) {
            val = map.getBoolean(key);
        } else {
            val = compute.test(key);
            map.put(key, val);
        }
        return val;
    }
}
