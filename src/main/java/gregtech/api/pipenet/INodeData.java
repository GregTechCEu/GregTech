package gregtech.api.pipenet;

import java.util.Set;

public interface INodeData<T extends INodeData<?>> {

    default double getWeightFactor() {
        return 1;
    }

    /**
     * Note - since datas is a set, no summative operations are allowed.
     */
    T getMinData(Set<T> datas);
}
