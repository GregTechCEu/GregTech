package gregtech.common.terminal.app.settings.widgets;

import gregtech.api.gui.resources.*;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.ColorWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.function.Consumer;

public class ThemeSettings extends AbstractWidgetGroup {
    private final WidgetGroup textureGroup;
    final TerminalOSWidget os;

    public ThemeSettings(TerminalOSWidget os) {
        super(Position.ORIGIN, new Size(323, 212));
        this.os = os;
        float x = 323 * 1.0f / 13;
        int y = 40;
        this.addWidget(new LabelWidget(323 / 2, 10, "terminal.settings.theme.color", -1).setXCentered(true));
        this.addColorButton(TerminalTheme.COLOR_1, "COLOR_1", (int) x, y);
        this.addColorButton(TerminalTheme.COLOR_2, "COLOR_2", (int) (x * 2), y);
        this.addColorButton(TerminalTheme.COLOR_3, "COLOR_3", (int) (x * 3), y);
        this.addColorButton(TerminalTheme.COLOR_4, "COLOR_4", (int) (x * 4), y);
        this.addColorButton(TerminalTheme.COLOR_5, "COLOR_5", (int) (x * 5), y);
        this.addColorButton(TerminalTheme.COLOR_6, "COLOR_6", (int) (x * 6), y);
        this.addColorButton(TerminalTheme.COLOR_7, "COLOR_7", (int) (x * 7), y);
        this.addColorButton(TerminalTheme.COLOR_F_1, "COLOR_F_1", (int) (x * 8), y);
        this.addColorButton(TerminalTheme.COLOR_F_2, "COLOR_F_2", (int) (x * 9), y);
        this.addColorButton(TerminalTheme.COLOR_B_1, "COLOR_B_1", (int) (x * 10), y);
        this.addColorButton(TerminalTheme.COLOR_B_2, "COLOR_B_2", (int) (x * 11), y);
        this.addColorButton(TerminalTheme.COLOR_B_3, "COLOR_B_3", (int) (x * 12), y);
        this.addWidget(new LabelWidget(323 / 2, 75, "terminal.settings.theme.wallpaper", -1).setXCentered(true));
        this.addWidget(new ImageWidget((int) x, 95, 150, 105, TerminalTheme.WALL_PAPER).setBorder(2, -1));
        this.addWidget(new SelectorWidget((int) (x + 170), 95, 116, 20,
                Arrays.asList(
                        "terminal.settings.theme.wallpaper.resource",
                        "terminal.settings.theme.wallpaper.url",
                        "terminal.settings.theme.wallpaper.color",
                        "terminal.settings.theme.wallpaper.file"),
                -1, this::getLocalizedWallpaperTypeName, true)
                .setIsUp(true)
                .setOnChanged(this::onModifyTextureChanged)
                .setColors(TerminalTheme.COLOR_B_2.getColor(), TerminalTheme.COLOR_F_1.getColor(), TerminalTheme.COLOR_B_2.getColor())
                .setBackground(TerminalTheme.COLOR_6));
        textureGroup = new WidgetGroup((int) (x + 170), 122, (int) (x * 11 - 170), 65);
        this.addWidget(textureGroup);
    }

    private String getLocalizedWallpaperTypeName(){
        switch(TerminalTheme.WALL_PAPER.getTypeName()){
            case "resource": return "terminal.settings.theme.wallpaper.resource";
            case "url": return "terminal.settings.theme.wallpaper.url";
            case "color": return "terminal.settings.theme.wallpaper.color";
            case "file": return "terminal.settings.theme.wallpaper.file";
        }
        return null;
    }

    private void addColorButton(ColorRectTexture texture, String name, int x, int y) {
        CircleButtonWidget buttonWidget = new CircleButtonWidget(x, y, 8, 1, 0).setFill(texture.getColor()).setStrokeAnima(-1).setHoverText(name);
        buttonWidget.setClickListener(cd -> TerminalDialogWidget.showColorDialog(os, name, color -> {
            if (color != null) {
                buttonWidget.setFill(color);
                texture.setColor(color);
                if (!TerminalTheme.saveConfig()) {
                    TerminalDialogWidget.showInfoDialog(os, "terminal.component.error", "terminal.component.save_file.error").setClientSide().open();
                }
            }
        }, texture.color).setClientSide().open());
        addWidget(buttonWidget);
    }

    private void onModifyTextureChanged(String type) {
        textureGroup.clearAllWidgets();
        switch (type) {
            case "terminal.settings.theme.wallpaper.resource":
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof TextureArea)) {
                    TerminalTheme.WALL_PAPER.setTexture(new TextureArea(new ResourceLocation("gregtech:textures/gui/terminal/terminal_background.png"), 0.0, 0.0, 1.0, 1.0));
                    TerminalTheme.saveConfig();
                }
                addStringSetting(((TextureArea)TerminalTheme.WALL_PAPER.getTexture()).imageLocation.toString(),
                        s -> {
                            TerminalTheme.WALL_PAPER.setTexture(new TextureArea(new ResourceLocation(s), 0.0, 0.0, 1.0, 1.0));
                            TerminalTheme.saveConfig();
                        });
                break;
            case "terminal.settings.theme.wallpaper.url":
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof URLTexture)) {
                    TerminalTheme.WALL_PAPER.setTexture(new URLTexture(null));
                    TerminalTheme.saveConfig();
                }
                addStringSetting(((URLTexture)TerminalTheme.WALL_PAPER.getTexture()).url, s -> {
                    TerminalTheme.WALL_PAPER.setTexture(new URLTexture(s));
                    TerminalTheme.saveConfig();
                });
                break;
            case "terminal.settings.theme.wallpaper.color":
                ColorRectTexture texture;
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof ColorRectTexture)) {
                    texture = new ColorRectTexture(-1);
                    TerminalTheme.WALL_PAPER.setTexture(texture);
                    TerminalTheme.saveConfig();
                } else {
                    texture = (ColorRectTexture) TerminalTheme.WALL_PAPER.getTexture();
                }
                textureGroup.addWidget(new ColorWidget(0, 0, 80, 10)
                        .setColorSupplier(texture::getColor, true)
                        .setOnColorChanged(texture::setColor));
                break;
            case "terminal.settings.theme.wallpaper.file":
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof FileTexture)) {
                    TerminalTheme.WALL_PAPER.setTexture(new FileTexture(null));
                    TerminalTheme.saveConfig();
                }
                textureGroup.addWidget(new RectButtonWidget(0, 0, 116, 20)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(),
                                TerminalTheme.COLOR_1.getColor(),
                                TerminalTheme.COLOR_B_1.getColor())
                        .setClickListener(cd-> TerminalDialogWidget.showFileDialog(os, "terminal.settings.theme.image", TerminalRegistry.TERMINAL_PATH, true, file->{
                            if (file != null && file.isFile()) {
                                TerminalTheme.WALL_PAPER.setTexture(new FileTexture(file));
                                TerminalTheme.saveConfig();
                            }
                        }).setClientSide().open())
                        .setIcon(new TextTexture("terminal.settings.theme.select", -1)));
                break;
        }
    }

    private void addStringSetting(String init, Consumer<String> callback) {
        TextFieldWidget textFieldWidget = new TextFieldWidget(0, 0, 76, 20, TerminalTheme.COLOR_B_2, null, null)
                .setMaxStringLength(Integer.MAX_VALUE)
                .setValidator(s -> true)
                .setCurrentString(init == null ? "" : init);
        textureGroup.addWidget(textFieldWidget);
        textureGroup.addWidget(new RectButtonWidget(76, 0, 40, 20)
                .setColors(TerminalTheme.COLOR_B_1.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_1.getColor())
                .setClickListener(cd->callback.accept(textFieldWidget.getCurrentString()))
                .setIcon(new TextTexture("terminal.guide_editor.update", -1)));
    }
}
