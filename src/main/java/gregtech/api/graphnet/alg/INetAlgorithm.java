package gregtech.api.graphnet.alg;

import gregtech.api.graphnet.alg.iter.IteratorFactory;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.path.NetPath;

public interface INetAlgorithm {

    <Path extends NetPath<?, ?>> IteratorFactory<Path> getPathsIteratorFactory(GraphVertex source,
                                                                               NetPathMapper<Path> remapper);
}
