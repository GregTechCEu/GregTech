package gregtech.api.graphnet.path;

import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface PathBuilder {

    @Contract("_, _ -> this")
    PathBuilder addToEnd(@NotNull NetNode node, @NotNull NetEdge edge);

    @Contract("_, _ -> this")
    PathBuilder addToStart(@NotNull NetNode node, @NotNull NetEdge edge);

    @Contract("-> this")
    PathBuilder reverse();

    NetPath build();
}
