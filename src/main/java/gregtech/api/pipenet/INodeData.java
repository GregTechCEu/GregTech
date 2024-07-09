package gregtech.api.pipenet;

import java.util.Collections;
import java.util.List;

public interface INodeData<T extends INodeData<?>> {

    default int getChannelMaxCount() {
        return 1;
    }

    default double getWeightFactor() {
        return 1;
    }

    default int getThroughput() {
        return 1;
    }

    T getSumData(List<T> datas);

    default T getSumData(Object data) {
        return getSumData(Collections.singletonList((T) data));
    }
}
