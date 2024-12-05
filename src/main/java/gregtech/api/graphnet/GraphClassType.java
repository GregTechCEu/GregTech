package gregtech.api.graphnet;

import gregtech.api.graphnet.logic.NetLogicEntry;

import gregtech.api.graphnet.net.IGraphNet;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class GraphClassType<T> implements IStringSerializable {

    private final @NotNull String name;
    private final @NotNull Function<IGraphNet, T> supplier;

    public GraphClassType(@NotNull ResourceLocation name, @NotNull Function<IGraphNet, T> supplier) {
        this.name = name.toString();
        this.supplier = supplier;

    }

    public GraphClassType(@NotNull String namespace, @NotNull String name, @NotNull Function<IGraphNet, T> supplier) {
        this.name = namespace + ":" + name;
        this.supplier = supplier;
    }

    public final T getNew(@NotNull IGraphNet net) {
        return supplier.apply(net);
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }
}
