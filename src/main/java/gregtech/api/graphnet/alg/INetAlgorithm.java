package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.INetPath;

import java.util.Iterator;

public interface INetAlgorithm {

    <Path extends INetPath<?, ?>> Iterator<Path> getPathsIterator(GraphVertex source, NetPathMapper<Path> remapper);
}
