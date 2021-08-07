package gregtech.api.terminal.gui.widgets.os;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.io.File;

public class TerminalMenuWidget extends WidgetGroup {
    private Interpolator interpolator;
    private IGuiTexture background;
    private final TerminalOSWidget os;
    public boolean isHide;


    public TerminalMenuWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        this.os = os;
        this.addWidget(new CircleButtonWidget(5, 10, 4, 1, 0)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(239, 105, 105).getRGB())
                .setHoverText("close")
                .setClickListener(this::close));
        this.addWidget(new CircleButtonWidget(15, 10, 4, 1, 0)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(243, 217, 117).getRGB())
                .setHoverText("minimize")
                .setClickListener(this::minimize));
        this.addWidget(new CircleButtonWidget(25, 10, 4, 1, 0)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(154, 243, 122).getRGB())
                .setHoverText("maximize")
                .setClickListener(this::maximize));
        this.addWidget(new CircleButtonWidget(15, 40, 10, 1, 14)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 255, 255).getRGB(),
                        new Color(80, 80, 80).getRGB())
                .setHoverText("setting")
                .setIcon(GuiTextures.TERMINAL_SETTING)
                .setClickListener(this::setting));
    }

    public TerminalMenuWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void close(ClickData clickData) {
        os.closeApplication(os.focusApp, clickData.isClient);
    }

    public void minimize(ClickData clickData) {
        os.minimizeApplication(os.focusApp, clickData.isClient);
    }

    public void maximize(ClickData clickData) {
        TerminalDialogWidget.showColorDialog(os, "test", System.out::println).addPlayerInventory().open();
    }

    public void setting(ClickData clickData) {
//        TerminalDialogWidget.showInfoDialog(os, "test");
//        TerminalDialogWidget.showConfirmDialog(os, "test", null);
//        TerminalDialogWidget.showTextFieldDialog(os, "test", s->true, System.out::println);
//        TerminalDialogWidget.showFileDialog(os, "test", new File("./"), System.out::println).setClientSide().open();
    }

    public void hideMenu() {
        if (!isHide && interpolator == null) {
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x - getSize().width, 10, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> {
                        setVisible(false);
                        interpolator = null;
                        isHide = true;
                    });
            interpolator.start();
        }
    }

    public void showMenu() {
        if (isHide && interpolator == null) {
            setVisible(true);
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x + getSize().width, 10, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> {
                        interpolator = null;
                        isHide = false;
                    });
            interpolator.start();
        }
    }

    @Override
    public void updateScreen() {
        if(interpolator != null) interpolator.update();
        super.updateScreen();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.color(1,1,1,0.5f);
        if( background != null) {
            background.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
        } else {
            drawGradientRect(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height, 0xff000000, 0xff000000);
        }
        GlStateManager.color(1,1,1,1);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
