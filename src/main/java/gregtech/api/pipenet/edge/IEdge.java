package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.predicate.IPredicateTestObject;

public interface IEdge<V> {

    V getSource();

    V getTarget();

    double getDynamicWeight(IPredicateTestObject channel, SimulatorKey simulator, long queryTick);
}
