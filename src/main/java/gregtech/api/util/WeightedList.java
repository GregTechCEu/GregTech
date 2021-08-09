package gregtech.api.util;

import java.util.*;
import java.util.stream.Collectors;

public class WeightedList<T extends IWeighted> {

    protected final ArrayList<T> list = new ArrayList<>();
    private int totalWeight = 0;

    public WeightedList() {
    }

    public WeightedList(Collection<? extends T> objects) {
        objects.forEach(this::add);
    }

    public WeightedList<T> add(T object) {
        this.list.add(object);
        this.totalWeight += object.getWeight();
        return this;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<T> getAllObjects() {
        return (List<T>) list.clone();
    }

    public T getRandomObject(Random random) {
        if (this.totalWeight == 0) return null;
        if (this.list.size() == 1) return this.list.get(0);
        int n = random.nextInt(this.totalWeight);
        for (T object : this.list) {
            if ((n -= object.getWeight()) < 0)
                return object;
        }
        return null;
    }

    public static class WeightedWrapperList<T> extends WeightedList<IWeighted.Wrapper<T>> {

        public WeightedWrapperList() {
        }

        public WeightedWrapperList(Map<? extends T, Integer> map) {
            map.forEach(this::add);
        }

        public WeightedWrapperList<T> add(T object, int weight) {
            super.add(new IWeighted.Wrapper<>(object, weight));
            return this;
        }

        public List<T> fetchAllObjects() {
            return list.stream().map(IWeighted.Wrapper::getObject).collect(Collectors.toList());
        }

        public T fetchRandomObject(Random random) {
            return getRandomObject(random).getObject();
        }

        public static <T> WeightedWrapperList<T> mergeWeightedList(WeightedWrapperList<WeightedWrapperList<T>> list) {
            WeightedWrapperList<T> l = new WeightedWrapperList<>();
            list.list.forEach(c -> c.getObject().list.forEach(o -> l.add(o.getObject(), o.getWeight() * c.getWeight())));
            return l;
        }
    }
}
