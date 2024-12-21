package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface NetIterator extends Iterator<NetNode> {

    boolean hasSeen(@NotNull NetNode node);

    @Nullable
    NetEdge getSpanningTreeEdge(@NotNull NetNode node);
}
