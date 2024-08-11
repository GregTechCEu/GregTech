package gregtech.api.graphnet.alg.iter;

import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface IteratorFactory<T> {

    /**
     * The provided iterator must always be used and discarded before the end of the tick.
     */
    @Contract("_, _, _, _ -> new")
    Iterator<T> newIterator(INetGraph graph, IPredicateTestObject testObject, @Nullable SimulatorKey simulator,
                            long queryTick);

    static void defaultPrepareRun(INetGraph graph, IPredicateTestObject testObject, @Nullable SimulatorKey simulator,
                                  long queryTick) {
        graph.prepareForAlgorithmRun(testObject, simulator, queryTick);
    }

    default boolean cacheable() {
        return true;
    }
}
