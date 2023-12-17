package gregtech.api.pipenet;

import java.util.Set;

public interface INodeData<T extends INodeData<?>> {

    default double getWeightFactor() {
        return 1;
    }

    T getMinData(Set<T> datas);
}
