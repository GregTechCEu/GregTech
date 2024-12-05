package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.edge.NetEdge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface NetIterator extends Iterator<NetNode> {

    @Nullable
    NetEdge getSpanningTreeEdge(@NotNull NetNode node);
}
