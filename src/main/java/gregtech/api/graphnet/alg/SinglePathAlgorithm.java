package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.path.INetPath;
import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.alg.iter.SimpleCacheableIterator;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SinglePathAlgorithm implements INetAlgorithm {

    private final IGraphNet net;

    public SinglePathAlgorithm(IGraphNet pipenet) {
        this.net = pipenet;
    }

    @Override
    public <Path extends INetPath<?, ?>> Iterator<Path> getPathsIterator(GraphVertex source,
                                                                         NetPathMapper<Path> remapper) {
        if (!this.net.getGraph().containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<GraphEdge> graphEdges = new ObjectArrayList<>();
        List<GraphVertex> nodes = new ObjectArrayList<>();
        nodes.add(source);
        GraphVertex lastNode = null;
        GraphVertex node = source;
        GraphEdge graphEdge;
        double sumWeight = 0;
        boolean valid = true;
        while (valid) {
            Iterator<GraphEdge> i = this.net.getGraph().outgoingEdgesOf(node).iterator();
            if (!i.hasNext()) break; // we've reached the end, exit the loop while still valid
            graphEdge = i.next();
            // if we are directed, we know that the target is the target.
            // if we aren't directed, we need to see if the graphEdge's source was secretly the target
            boolean reversedEdge = !this.net.getGraph().isDirected() && getSource(graphEdge) == lastNode;
            if (getTarget(graphEdge) == lastNode || reversedEdge) {
                if (i.hasNext()) graphEdge = i.next();
                else break; // we've reached the end, exit the loop while still valid
            } else if (i.hasNext()) i.next();
            if (i.hasNext()) valid = false; // third graphEdge detected - that's an invalid group
            lastNode = node;
            node = reversedEdge ? getSource(graphEdge) : getTarget(graphEdge);
            graphEdges.add(graphEdge);
            nodes.add(node);
            sumWeight += getWeight(graphEdge);
        }
        if (!valid) return Collections.emptyIterator();
        return new SimpleCacheableIterator<>(ImmutableList.of(remapper.map(nodes, graphEdges, sumWeight)));
    }

    private GraphVertex getSource(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeSource(graphEdge);
    }

    private GraphVertex getTarget(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeTarget(graphEdge);
    }

    private double getWeight(GraphEdge graphEdge) {
        return this.net.getGraph().getEdgeWeight(graphEdge);
    }
}
