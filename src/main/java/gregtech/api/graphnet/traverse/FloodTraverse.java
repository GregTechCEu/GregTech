package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;

import org.jgrapht.Graph;
import org.jgrapht.traverse.ClosestFirstIterator;

public class FloodTraverse {

    protected static class Iterator extends ClosestFirstIterator<GraphVertex, GraphEdge> {

        public Iterator(Graph<GraphVertex, GraphEdge> g, GraphVertex startVertex) {
            super(g, startVertex);
        }
    }
}
