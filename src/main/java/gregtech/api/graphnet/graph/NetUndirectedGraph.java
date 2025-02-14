package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.net.IGraphNet;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetUndirectedGraph extends BaseNetGraph {

    public NetUndirectedGraph(Supplier<GraphVertex> vertexSupplier, Supplier<GraphEdge> edgeSupplier) {
        super(vertexSupplier, edgeSupplier, false);
    }

    public static Function<IGraphNet, INetGraph> standardBuilder() {
        return iGraphNet -> new NetUndirectedGraph(
                () -> new GraphVertex(iGraphNet.getDefaultNodeType().getNew(iGraphNet)),
                () -> new GraphEdge(iGraphNet.getDefaultEdgeType().getNew(iGraphNet)));
    }
}
