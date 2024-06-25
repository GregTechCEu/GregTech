package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.WorldPipeNetBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class SinglePathAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
        E extends NetEdge> implements INetAlgorithm<PT, NDT, E> {

    private final Graph<NetNode<PT, NDT, E>, E> graph;
    private final boolean isDirected;

    public SinglePathAlgorithm(WorldPipeNetBase<NDT, PT, E> pipenet) {
        this.graph = pipenet.getGraph();
        this.isDirected = pipenet.isDirected();
    }

    @Override
    public List<NetPath<PT, NDT, E>> getPathsList(NetNode<PT, NDT, E> source) {
        if (!this.graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<E> edges = new ObjectArrayList<>();
        List<NetNode<PT, NDT, E>> nodes = new ObjectArrayList<>();
        nodes.add(source);
        NetNode<PT, NDT, E> lastNode = null;
        NetNode<PT, NDT, E> node = source;
        E edge;
        double sumWeight = source.getData().getWeightFactor();
        boolean valid = true;
        while (valid) {
            Iterator<E> i = this.graph.outgoingEdgesOf(node).iterator();
            if (!i.hasNext()) break; // we've reached the end, exit the loop while still valid
            edge = i.next();
            // if we are directed, we know that the target is the target.
            // if we aren't directed, we need to see if the edge's source was secretly the target
            boolean reversedEdge = !isDirected && edge.getSource() == lastNode;
            if (edge.getTarget() == lastNode || reversedEdge) {
                if (i.hasNext()) edge = i.next();
                else break; // we've reached the end, exit the loop while still valid
            } else if (i.hasNext()) i.next();
            if (i.hasNext()) valid = false; // third edge detected - that's an invalid group
            lastNode = node;
            node = reversedEdge ? edge.getCastSource() : edge.getCastTarget();
            edges.add(edge);
            nodes.add(node);
            sumWeight += node.getData().getWeightFactor();
        }
        if (!valid) return Collections.emptyList();
        return ImmutableList.of(new NetPath<>(nodes, edges, sumWeight));
    }
}
