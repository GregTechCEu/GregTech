package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.IGraphNet;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetDirectedGraph extends BaseNetGraph {

    public NetDirectedGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier) {
        super(vertexSupplier, edgeSupplier, true);
    }

    public static Function<IGraphNet, INetGraph> standardBuilder() {
        return iGraphNet -> new NetDirectedGraph(
                () -> new GraphVertex(iGraphNet.getDefaultNodeType().getNew(iGraphNet)),
                () -> new GraphEdge(iGraphNet.getDefaultEdgeType().getNew(iGraphNet)));
    }
}
