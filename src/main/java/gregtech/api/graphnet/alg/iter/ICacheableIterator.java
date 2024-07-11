package gregtech.api.graphnet.alg.iter;

import java.util.Iterator;

public interface ICacheableIterator<T> extends Iterator<T> {

    ICacheableIterator<T> newCacheableIterator();

    Iterator<T> newIterator();
}
