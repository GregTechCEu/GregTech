package gregtech.api.graphnet.graph;

import org.jgrapht.Graph;

public interface INetGraph extends Graph<GraphVertex, GraphEdge> {

    boolean isDirected();
}
