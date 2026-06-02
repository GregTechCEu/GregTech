package gregtech.api.util.collection;

import java.util.Map;

public interface MapFunction {

    <K, V> Map<K, V> createMap(int size);
}
