package gregtech.api.terminal.app;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public class ThemeSettingApp extends AbstractApplication {
    public ThemeSettingApp() {
        super("Theme Setting", GuiTextures.GREGTECH_LOGO);
    }

    @Override
    public AbstractApplication createApp(boolean isClient, NBTTagCompound nbt) {
        ThemeSettingApp app = new ThemeSettingApp();
        if (isClient) { //333 232
            float x = 333 * 1.0f / 13;
            app.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            app.addColorButton(TerminalTheme.COLOR_1, "COLOR_1", (int) x, 30);
            app.addColorButton(TerminalTheme.COLOR_2, "COLOR_2", (int) (x * 2), 30);
            app.addColorButton(TerminalTheme.COLOR_3, "COLOR_3", (int) (x * 3), 30);
            app.addColorButton(TerminalTheme.COLOR_4, "COLOR_4", (int) (x * 4), 30);
            app.addColorButton(TerminalTheme.COLOR_5, "COLOR_5", (int) (x * 5), 30);
            app.addColorButton(TerminalTheme.COLOR_6, "COLOR_6", (int) (x * 6), 30);
            app.addColorButton(TerminalTheme.COLOR_7, "COLOR_7", (int) (x * 7), 30);
            app.addColorButton(TerminalTheme.COLOR_F_1, "COLOR_F_1", (int) (x * 8), 30);
            app.addColorButton(TerminalTheme.COLOR_F_2, "COLOR_F_2", (int) (x * 9), 30);
            app.addColorButton(TerminalTheme.COLOR_B_1, "COLOR_B_1", (int) (x * 10), 30);
            app.addColorButton(TerminalTheme.COLOR_B_2, "COLOR_B_2", (int) (x * 11), 30);
            app.addColorButton(TerminalTheme.COLOR_B_3, "COLOR_B_3", (int) (x * 12), 30);
            app.addWidget(new ImageWidget((int) x, 80, 333 / 2, 232 / 2, TerminalTheme.WALL_PAPER));
        }
        return app;
    }

    private void addColorButton(ColorRectTexture texture, String name, int x, int y) {
        CircleButtonWidget buttonWidget = new CircleButtonWidget(x, y).setFill(texture.getColor()).setStrokeAnima(-1).setHoverText(name);
        buttonWidget.setClickListener(cd -> {
            TerminalDialogWidget.showColorDialog(getOs(), name, color -> {
                if (color != null) {
                    buttonWidget.setFill(color);
                    texture.setColor(color);
                    if (!TerminalTheme.saveConfig()) {
                        TerminalDialogWidget.showInfoDialog(getOs(), "ERROR", "error while saving config").setClientSide().open();
                    }
                }
            }).setClientSide().open();
        });
        addWidget(buttonWidget);
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
    }
}
