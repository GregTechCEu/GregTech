package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NetIteratorSupplier {

    @NotNull
    NetIterator create(@NotNull NetNode origin, @NotNull EdgeSelector direction);
}
