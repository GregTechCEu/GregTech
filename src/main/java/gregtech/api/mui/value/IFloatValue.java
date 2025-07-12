package gregtech.api.mui.value;

import com.cleanroommc.modularui.api.value.IValue;

public interface IFloatValue<T> extends IValue<T> {

    float getFloatValue();

    void setFloatValue(float val);
}
