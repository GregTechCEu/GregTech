package gregtech.api.mui.sync;

import gregtech.api.mui.value.IFloatValue;

import com.cleanroommc.modularui.api.value.sync.IValueSyncHandler;

public interface IFloatSyncValue<T> extends IValueSyncHandler<T>, IFloatValue<T> {

    @Override
    default void setFloatValue(float value) {
        setFloatValue(value, true, true);
    }

    default void setFloatValue(float value, boolean setSource) {
        setFloatValue(value, setSource, true);
    }

    void setFloatValue(float value, boolean setSource, boolean sync);
}
