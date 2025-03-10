package gregtech.common.covers.filter;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class MergabilityInfo<T extends IPredicateTestObject> {

    private final Object2ObjectLinkedOpenHashMap<T, Merge> mergeMap = new Object2ObjectLinkedOpenHashMap<>();

    public void add(int handlerSlot, T testObject, int count) {
        Merge merge = mergeMap.computeIfAbsent(testObject, Merge::new);
        merge.count += count;
        merge.handlerSlots.add(handlerSlot);
    }

    public @NotNull Collection<Merge> getMerges() {
        return mergeMap.values();
    }

    public final class Merge {

        private final T testObject;

        private int count = 0;
        private final IntList handlerSlots = new IntArrayList();

        public Merge(T testObject) {
            this.testObject = testObject;
        }

        public int getCount() {
            return count;
        }

        public T getTestObject() {
            return testObject;
        }

        public IntList getHandlerSlots() {
            return handlerSlots;
        }
    }
}
