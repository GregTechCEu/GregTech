package gregtech.api.util;

import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MCGuiUtil {

    public static GuiResponder createTextFieldResponder(Consumer<String> onChanged) {
        return new GuiResponder() {
            @Override
            public void setEntryValue(int id, boolean value) {
            }

            @Override
            public void setEntryValue(int id, float value) {
            }

            @Override
            public void setEntryValue(int id, @Nonnull String value) {
                onChanged.accept(value);
            }
        };
    }


}
