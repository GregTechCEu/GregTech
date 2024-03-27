package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.Graph;

import java.util.Iterator;
import java.util.List;

public final class SinglePathAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>>
                                      implements NetAlgorithm<PT, NDT> {

    private final Graph<NodeG<PT, NDT>, NetEdge> graph;
    private final boolean isDirected;

    public SinglePathAlgorithm(Graph<NodeG<PT, NDT>, NetEdge> graph, boolean isDirected) {
        this.graph = graph;
        this.isDirected = isDirected;
    }

    @Override
    public List<NetPath<PT, NDT>> getPathsList(NodeG<PT, NDT> source) {
        if (!this.graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph must contain the source vertex");
        }
        List<NetPath<PT, NDT>> paths = new ObjectArrayList<>();
        List<NetEdge> edges = new ObjectArrayList<>();
        List<NodeG<PT, NDT>> nodes = new ObjectArrayList<>();
        nodes.add(source);
        NodeG<PT, NDT> lastNode = null;
        NodeG<PT, NDT> node = source;
        NetEdge edge;
        double sumWeight = source.getData().getWeightFactor();
        boolean valid = true;
        while (valid) {
            Iterator<NetEdge> i = this.graph.outgoingEdgesOf(node).iterator();
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
            node = (NodeG<PT, NDT>) (reversedEdge ? edge.getSource() : edge.getTarget());
            edges.add(edge);
            nodes.add(node);
            sumWeight += node.getData().getWeightFactor();
        }
        if (!valid) return paths;
        paths.add(new NetPath<>(nodes, edges, sumWeight));
        return paths;
    }
}
