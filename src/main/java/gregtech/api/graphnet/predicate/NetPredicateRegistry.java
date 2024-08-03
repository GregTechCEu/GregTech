package gregtech.api.graphnet.predicate;

import gregtech.api.graphnet.logic.NetLogicEntry;

import gregtech.api.util.function.NullableSupplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public final class NetPredicateRegistry {

    private static final Map<String, NullableSupplier<EdgePredicate<?, ?>>> REGISTRY = new Object2ObjectOpenHashMap<>();

    static void register(EdgePredicate<?, ?> predicate) {
        REGISTRY.putIfAbsent(predicate.getName(), predicate::getNew);
    }

    public static @Nullable Supplier<EdgePredicate<?, ?>> getSupplierNullable(String name) {
        return REGISTRY.get(name);
    }

    public static @NotNull NullableSupplier<EdgePredicate<?, ?>> getSupplierNotNull(String name) {
        return REGISTRY.getOrDefault(name, () -> null);
    }

    public static @NotNull Supplier<EdgePredicate<?, ?>> getSupplierErroring(String name) {
        Supplier<EdgePredicate<? ,?>> supplier = REGISTRY.get(name);
        if (supplier == null) throwNonexistenceError();
        return supplier;
    }

    public static void throwNonexistenceError() {
        throw new RuntimeException("Could not find a matching supplier for an encoded EdgePredicate. " +
                "This suggests that the server and client have different GT versions or modifications.");
    }
}
