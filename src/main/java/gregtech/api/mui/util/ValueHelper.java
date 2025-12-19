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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public final class ValueHelper {

    public static BoolValue.Dynamic boolValueOf(@NotNull IBoolValue<?> value, boolean target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getBoolValue() == target, $ -> value.setBoolValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(@NotNull IByteValue<?> value, byte target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getByteValue() == target, $ -> value.setByteValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(@NotNull IDoubleValue<?> value, double target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getDoubleValue() == target, $ -> value.setDoubleValue(target));
    }

    public static <T extends Enum<T>> BoolValue.Dynamic boolValueOf(@NotNull IEnumValue<T> value, T target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getValue() == target, $ -> value.setValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(@NotNull IIntValue<?> value, int target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getIntValue() == target, $ -> value.setIntValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(@NotNull ILongValue<?> value, long target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> value.getLongValue() == target, $ -> value.setLongValue(target));
    }

    public static BoolValue.Dynamic boolValueOf(@NotNull IStringValue<?> value, @UnknownNullability String target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> Objects.equals(value.getStringValue(), target),
                $ -> value.setStringValue(target));
    }

    public static <T> BoolValue.Dynamic boolValueOf(@NotNull IValue<T> value, @UnknownNullability T target) {
        Objects.requireNonNull(value);
        return new BoolValue.Dynamic(() -> Objects.equals(value.getValue(), target), $ -> value.setValue(target));
    }
}
