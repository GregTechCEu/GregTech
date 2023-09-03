package gregtech.api.newgui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.sync.IServerMouseAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.MouseData;
import net.minecraft.util.IStringSerializable;

import java.util.function.*;

public class GuiFunctions {

    public static IServerMouseAction getIncrementer(int normal, int shift, int ctrl, int shiftCtrl, IntConsumer consumer) {
        return (clickData) -> {
            int amount = normal;
            if (clickData.shift) {
                if (clickData.ctrl)
                    amount = shiftCtrl;
                else
                    amount = shift;
            } else if (clickData.ctrl)
                amount = ctrl;
            consumer.accept(amount);
        };
    }

    public static <T extends Enum<T> & IStringSerializable> IntFunction<IDrawable> enumStringTextureGetter(Class<T> clazz) {
        return enumStringTextureGetter(clazz, IStringSerializable::getName);
    }

    public static <T extends Enum<T>> IntFunction<IDrawable> enumStringTextureGetter(Class<T> clazz, Function<T, String> nameGetter) {
        return val -> {
            T[] values = clazz.getEnumConstants();
            if (val >= values.length) {
                throw new ArrayIndexOutOfBoundsException("Tried getting enum constant of class " + clazz.getSimpleName() + " at index " + val);
            }
            return IKey.lang(nameGetter.apply(values[val]));
        };
    }
}
