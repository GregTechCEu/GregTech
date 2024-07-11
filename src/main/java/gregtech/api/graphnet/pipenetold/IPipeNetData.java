package gregtech.api.graphnet.pipenetold;

import gregtech.api.graphnet.logic.NetLogicData;

import java.util.Collections;
import java.util.List;

public interface IPipeNetData<T extends IPipeNetData<T>> extends NetLogicData {

    T getSumData(List<T> datas);

    default T getSumData(Object data) {
        return getSumData(Collections.singletonList((T) data));
    }
}
