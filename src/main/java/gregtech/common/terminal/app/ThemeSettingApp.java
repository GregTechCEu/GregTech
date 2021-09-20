package gregtech.common.terminal.app;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.*;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.ColorWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.function.Consumer;

public class ThemeSettingApp extends AbstractApplication {
    public ThemeSettingApp() {
        super("theme_settings");
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.GREGTECH_LOGO;
    }

    private WidgetGroup textureGroup;

    @Override
    public AbstractApplication initApp() {
        if (isClient) { //333 232
            float x = 333 * 1.0f / 13;
            int y = 50;
            this.addWidget(new ImageWidget(5, 5, 333 - 10, 232 - 10, TerminalTheme.COLOR_B_2));
            this.addWidget(new LabelWidget(333 / 2, 20, "terminal.theme_settings.color", -1).setXCentered(true));
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
            this.addWidget(new LabelWidget(333 / 2, 85, "terminal.theme_settings.wallpaper", -1).setXCentered(true));
            this.addWidget(new ImageWidget((int) x, 105, 150, 105, TerminalTheme.WALL_PAPER).setBorder(2, -1));
            this.addWidget(new SelectorWidget((int) (x + 170), 105, 116, 20, Arrays.asList("resource", "url", "color", "file"), -1, TerminalTheme.WALL_PAPER::getTypeName, true)
                    .setIsUp(true)
                    .setOnChanged(type->onModifyTextureChanged(type, this))
                    .setColors(TerminalTheme.COLOR_B_2.getColor(), TerminalTheme.COLOR_F_1.getColor(), TerminalTheme.COLOR_B_2.getColor())
                    .setBackground(TerminalTheme.COLOR_B_1));
            textureGroup = new WidgetGroup((int) (x + 170), 132, (int) (x * 11 - 170), 65);
            this.addWidget(textureGroup);
        }
        return this;
    }

    private void addColorButton(ColorRectTexture texture, String name, int x, int y) {
        CircleButtonWidget buttonWidget = new CircleButtonWidget(x, y, 8, 1, 0).setFill(texture.getColor()).setStrokeAnima(-1).setHoverText(name);
        buttonWidget.setClickListener(cd -> {
            TerminalDialogWidget.showColorDialog(getOs(), name, color -> {
                if (color != null) {
                    buttonWidget.setFill(color);
                    texture.setColor(color);
                    if (!TerminalTheme.saveConfig()) {
                        TerminalDialogWidget.showInfoDialog(getOs(), "terminal.component.error", "terminal.component.save_file.error").setClientSide().open();
                    }
                }
            }).setClientSide().open();
        });
        addWidget(buttonWidget);
    }

    private void onModifyTextureChanged(String type, AbstractApplication app) {
        textureGroup.clearAllWidgets();
        switch (type) {
            case "resource":
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
            case "url":
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof URLTexture)) {
                    TerminalTheme.WALL_PAPER.setTexture(new URLTexture(null));
                    TerminalTheme.saveConfig();
                }
                addStringSetting(((URLTexture)TerminalTheme.WALL_PAPER.getTexture()).url, s -> {
                    TerminalTheme.WALL_PAPER.setTexture(new URLTexture(s));
                    TerminalTheme.saveConfig();
                });
                break;
            case "color":
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
            case "file":
                if (!(TerminalTheme.WALL_PAPER.getTexture() instanceof FileTexture)) {
                    TerminalTheme.WALL_PAPER.setTexture(new FileTexture(null));
                    TerminalTheme.saveConfig();
                }
                textureGroup.addWidget(new RectButtonWidget(0, 0, 116, 20)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(),
                                TerminalTheme.COLOR_1.getColor(),
                                TerminalTheme.COLOR_B_1.getColor())
                        .setClickListener(cd-> TerminalDialogWidget.showFileDialog(app.getOs(), "terminal.theme_settings.image", TerminalRegistry.TERMINAL_PATH, true, file->{
                            if (file != null && file.isFile()) {
                                TerminalTheme.WALL_PAPER.setTexture(new FileTexture(file));
                                TerminalTheme.saveConfig();
                            }
                        }).setClientSide().open())
                        .setIcon(new TextTexture("terminal.theme_settings.select", -1)));
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

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
