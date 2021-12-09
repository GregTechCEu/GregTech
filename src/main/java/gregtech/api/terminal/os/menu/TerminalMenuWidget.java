package gregtech.api.terminal.os.menu;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.SystemCall;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class TerminalMenuWidget extends WidgetGroup {
    @SideOnly(Side.CLIENT)
    private Interpolator interpolator;
    private IGuiTexture background;
    private final TerminalOSWidget os;
    private final List<Tuple<IMenuComponent, WidgetGroup>> components;
    public boolean isHide;


    public TerminalMenuWidget(Position position, Size size, TerminalOSWidget os) {
        super(position, size);
        addSelfPosition( -size.width, 0);
        setVisible(false);
        isHide = true;
        this.os = os;
        components = new ArrayList<>();
        this.addWidget(new CircleButtonWidget(5, 10, 4, 1, 0)
                .setColors(0,
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_3.getColor())
                .setHoverText("terminal.menu.close")
                .setClickListener(this::close));
        this.addWidget(new CircleButtonWidget(15, 10, 4, 1, 0)
                .setColors(0,
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_2.getColor())
                .setHoverText("terminal.menu.minimize")
                .setClickListener(this::minimize));
        this.addWidget(new CircleButtonWidget(25, 10, 4, 1, 0)
                .setColors(0,
                        TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_1.getColor())
                .setHoverText("terminal.menu.maximize")
                .setClickListener(this::maximize));
    }

    public TerminalMenuWidget setBackground(IGuiTexture background) {
        this.background = background;
        return this;
    }

    public void close(ClickData clickData) {
        SystemCall.CLOSE_FOCUS_APP.call(os, clickData.isClient);
    }

    public void minimize(ClickData clickData) {
        SystemCall.MINIMIZE_FOCUS_APP.call(os, clickData.isClient);
    }

    public void maximize(ClickData clickData) {
        SystemCall.FULL_SCREEN.call(os, clickData.isClient);
    }

    public void addComponent(IMenuComponent component) {
        WidgetGroup group = new WidgetGroup();
        int x = 15;
        int y = 40 + components.size() * 25;
        CircleButtonWidget button = new CircleButtonWidget(x, y, 10, 1, 16)
                .setColors(0, 0xFFFFFFFF, 0)
                .setHoverText(component.hoverText())
                .setIcon(component.buttonIcon());
        button.setClickListener(c->{
                    components.forEach(tuple -> {
                        if (tuple.getFirst() instanceof Widget && tuple.getFirst() != component){
                            ((Widget) tuple.getFirst()).setActive(false);
                            ((Widget) tuple.getFirst()).setVisible(false);
                            ((CircleButtonWidget) tuple.getSecond().widgets.get(0)).setFill(0);
                        }
                    });
                    if (component instanceof Widget) {
                        Widget widget = (Widget)component;
                        widget.setVisible(!widget.isVisible());
                        widget.setActive(!widget.isActive());
                        button.setFill(widget.isVisible() ? 0xFF94E2C1 : 0);
                    }
                    component.click(c);
                });
        group.addWidget(button);
        if (component instanceof Widget) {
            Widget widget = (Widget)component;
            widget.setSelfPosition(new Position(x + 20, 0));
            widget.setVisible(false);
            widget.setActive(false);
            group.addWidget(widget);
        }
        this.addWidget(group);
        components.add(new Tuple<>(component, group));
    }

    public void loadComponents(AbstractApplication app) {
        removeComponents();
        if (app != null) {
            app.getMenuComponents().forEach(this::addComponent);
        }
    }

    public void removeComponents() {
        components.forEach(component->this.removeWidget(component.getSecond()));
        components.clear();
    }

    @SideOnly(Side.CLIENT)
    public void hideMenu() {
        if (!isHide && interpolator == null) {
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x - getSize().width, 6, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> {
                        setVisible(false);
                        interpolator = null;
                        isHide = true;
                    });
            interpolator.start();
            os.desktop.removeTopWidget(this);
        }
    }

    @SideOnly(Side.CLIENT)
    public void showMenu() {
        if (isHide && interpolator == null) {
            setVisible(true);
            int y = getSelfPosition().y;
            interpolator = new Interpolator(getSelfPosition().x, getSelfPosition().x + getSize().width, 6, Eases.EaseLinear,
                    value-> setSelfPosition(new Position(value.intValue(), y)),
                    value-> {
                        interpolator = null;
                        isHide = false;
                    });
            interpolator.start();
            os.desktop.addTopWidget(this);
        }
    }

    @Override
    public void updateScreenOnFrame() {
        if(interpolator != null) interpolator.update();
        super.updateScreenOnFrame();
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

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!super.mouseClicked(mouseX, mouseY, button)) {
            if (isMouseOverElement(mouseX, mouseY)) {
                return true;
            } else if (!isHide) {
                hideMenu();
            }
            return false;
        }
        return true;
    }
}
