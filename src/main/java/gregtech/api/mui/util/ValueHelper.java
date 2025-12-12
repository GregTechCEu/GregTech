package gregtech.api.mui.util;

import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.value.IByteValue;
import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.ILongValue;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.value.BoolValue;

import java.util.Objects;

public final class ValueHelper {

    public static BoolValue.Dynamic boolValueOf(IBoolValue<?> value, boolean target) {
        return new BoolValue.Dynamic(() -> value.getBoolValue() == target, $ -> value.setBoolValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(IByteValue<?> value, byte target) {
        return new BoolValue.Dynamic(() -> value.getByteValue() == target, $ -> value.setByteValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(IDoubleValue<?> value, double target) {
        return new BoolValue.Dynamic(() -> value.getDoubleValue() == target, $ -> value.setDoubleValue(target));
    }

    public static <T extends Enum<T>> BoolValue.Dynamic boolValueOf(IEnumValue<T> value, T target) {
        return new BoolValue.Dynamic(() -> value.getValue() == target, $ -> value.setValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(IIntValue<?> value, int target) {
        return new BoolValue.Dynamic(() -> value.getIntValue() == target, $ -> value.setIntValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(ILongValue<?> value, long target) {
        return new BoolValue.Dynamic(() -> value.getLongValue() == target, $ -> value.setLongValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(IStringValue<?> value, String target) {
        return new BoolValue.Dynamic(() -> Objects.equals(value.getStringValue(), target),
                $ -> value.setStringValue(target));
    }

    public static <T> BoolValue.Dynamic boolValueOf(IValue<T> value, T target) {
        return new BoolValue.Dynamic(() -> Objects.equals(value.getValue(), target), $ -> value.setValue(target));
    }
}
