package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.predicate.IPredicateTestObject;

public interface IEdge<V> {

    V getSource();

    V getTarget();

    double getWeight(IPredicateTestObject channel, SimulatorKey simulator, long queryTick);
}
