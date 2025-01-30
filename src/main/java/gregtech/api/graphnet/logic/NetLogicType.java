package gregtech.api.graphnet.logic;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class NetLogicType<T extends NetLogicEntry<T, ?>> implements IStringSerializable {

    private final @NotNull String name;
    private final @NotNull Supplier<@NotNull T> supplier;
    private final @NotNull T defaultable;

    public NetLogicType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                        @NotNull T defaultable) {
        this.name = name.toString();
        this.supplier = supplier;
        this.defaultable = defaultable;
    }

    public NetLogicType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                        @NotNull T defaultable) {
        this.name = namespace + ":" + name;
        this.supplier = supplier;
        this.defaultable = defaultable;
    }

    @SuppressWarnings("unchecked")
    public T cast(NetLogicEntry<?, ?> entry) {
        return (T) entry;
    }

    public final @NotNull T getNew() {
        return supplier.get();
    }

    public final @NotNull T getDefault() {
        return defaultable;
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }
}
