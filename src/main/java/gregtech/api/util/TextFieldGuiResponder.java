package gregtech.api.util;

import net.minecraft.client.gui.GuiPageButtonList;

import java.util.function.Consumer;

public class TextFieldGuiResponder implements GuiPageButtonList.GuiResponder {

    private final Consumer<String> consumer;

    public TextFieldGuiResponder(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setEntryValue(int id, boolean value) { }

    @Override
    public void setEntryValue(int id, float value) { }

    @Override
    public void setEntryValue(int id, String value) {
        consumer.accept(value);
    }

}
