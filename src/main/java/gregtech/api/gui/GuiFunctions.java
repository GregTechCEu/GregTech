package gregtech.api.gui;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.common.widget.Widget;
import net.minecraft.util.IStringSerializable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class GuiFunctions {

    public static BiConsumer<Widget.ClickData, Widget> getIncrementer(int normal, int shift, int ctrl, int shiftCtrl, IntConsumer consumer) {
        return (clickData, widget) -> {
            int amount = 1;
            if (clickData.shift) {
                if (clickData.ctrl)
                    amount = 512;
                else
                    amount = 8;
            } else if (clickData.ctrl)
                amount = 64;
            consumer.accept(amount);
        };
    }

    public static <T extends Enum<T> & IStringSerializable> Function<Integer, IDrawable> enumStringTextureGetter(Class<T> clazz) {
        return val -> {
            T[] values = clazz.getEnumConstants();
            if (val >= values.length) {
                throw new ArrayIndexOutOfBoundsException("Tried getting enum constant of class " + clazz.getSimpleName() + " at index " + val);
            }
            return new Text(values[val].getName());
        };
    }
}
