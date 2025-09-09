package gregtech.api.recipes.logic;

import gregtech.api.util.GTLog;
import gregtech.api.util.GuardedData;

import gregtech.common.ConfigHolder;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class RecipeLogicCore {

    public static GuardedData<Map<MapKey<?>, Object>> createData() {
        return new GuardedData<>(Reference2ObjectOpenHashMap::new, m -> {
            Reference2ObjectOpenHashMap<MapKey<?>, Object> a = new Reference2ObjectOpenHashMap<>(Math.max(16, m.size()));
            a.putAll(m);
            return a;
        });
    }

    public static void stateError(@NotNull String message) {
        if (ConfigHolder.misc.ignoreRecipeLogicErrors) {
            GTLog.logger.error("Recipe logic has encountered a state error.");
            GTLog.logger.error(message);
            GTLog.logger.error("Ignoring this error due to config settings. If a stacktrace is desired, set the config to false.");
        } else {
            throw new IllegalStateException(message);
        }
    }
}
