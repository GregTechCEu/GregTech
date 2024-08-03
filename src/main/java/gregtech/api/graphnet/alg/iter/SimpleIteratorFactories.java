package gregtech.api.graphnet.alg.iter;

import com.cleanroommc.modularui.utils.math.functions.trig.Sin;

import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class SimpleIteratorFactories {

    public static <T> IteratorFactory<T> emptyFactory() {
        return (graph, testObject, simulator, queryTick) ->  new SingletonIterator<>(null);
    }

    public static <T> IteratorFactory<T> fromIterable(Iterable<T> prototype) {
        return (graph, testObject, simulator, queryTick) -> prototype.iterator();
    }

    public static <T> IteratorFactory<T> fromSingleton(T singleton) {
        return (graph, testObject, simulator, queryTick) -> new SingletonIterator<>(singleton);
    }

    public static <T> SingletonIterator<T> getSingletonIterator(T singleton) {
        return new SingletonIterator<>(singleton);
    }

    private static class SingletonIterator<T> implements Iterator<T> {

        private T singleton;

        public SingletonIterator(T singleton) {
            this.singleton = singleton;
        }

        @Override
        public boolean hasNext() {
            return singleton != null;
        }

        @Override
        public T next() {
            T temp = singleton;
            singleton = null;
            return temp;
        }
    }
}
