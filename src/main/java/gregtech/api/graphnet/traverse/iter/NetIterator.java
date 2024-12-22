package gregtech.api.graphnet.traverse.iter;

import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface NetIterator extends Iterator<NetNode> {

    /**
     * @param node the node in question.
     * @return Whether this iterator has encountered the node in question.
     */
    boolean hasSeen(@NotNull NetNode node);

    /**
     * @param node the node in question.
     * @return the next edge along the lowest weight path to the origin node for the node in question.
     */
    @Nullable
    NetEdge getSpanningTreeEdge(@NotNull NetNode node);
}
