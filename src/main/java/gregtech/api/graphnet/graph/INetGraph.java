package gregtech.api.graphnet.graph;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.ApiStatus;
import org.jgrapht.Graph;

public interface INetGraph extends Graph<GraphVertex, GraphEdge> {

    boolean isDirected();
}
