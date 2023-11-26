package gregtech.api.util;

import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * This class exists to avoid java always trying to load client classes when loading
 * {@link gregtech.api.gui.widgets.TextFieldWidget}.
 * Do not remove
 */
public class MCGuiUtil {

    public static GuiResponder createTextFieldResponder(Consumer<String> onChanged) {
        return new GuiResponder() {

            @Override
            public void setEntryValue(int id, boolean value) {}

            @Override
            public void setEntryValue(int id, float value) {}

            @Override
            public void setEntryValue(int id, @Nonnull String value) {
                onChanged.accept(value);
            }
        };
    }
}
