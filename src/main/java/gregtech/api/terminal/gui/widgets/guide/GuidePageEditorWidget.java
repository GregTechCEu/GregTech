package gregtech.api.terminal.gui.widgets.guide;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class GuidePageEditorWidget extends GuidePageWidget {
    private final Map<Widget, JsonObject> configMap;
    private Widget selected;
    private final WidgetGroup toolButtons;
    private GuideConfigEditor configEditor;

    public GuidePageEditorWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
        this.setDraggable(false);
        configMap = new HashMap<>();
        setTitle("Template");
        toolButtons = new WidgetGroup(Position.ORIGIN, Size.ZERO);
        toolButtons.setVisible(false);
        toolButtons.addWidget(new CircleButtonWidget(-20, -4, 8, 1, 12)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(88, 198, 88).getRGB(),
                        new Color(158, 238, 124).getRGB())
                .setIcon(GuiTextures.TERMINAL_UP)
                .setHoverText("up")
                .setClickListener(this::moveUp));
        toolButtons.addWidget(new CircleButtonWidget(0, -4, 8, 1, 12)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(255, 217, 0).getRGB(),
                        new Color(243, 217, 117).getRGB())
                .setIcon(GuiTextures.TERMINAL_DOWN)
                .setHoverText("down")
                .setClickListener(this::moveDown));
        toolButtons.addWidget(new CircleButtonWidget(20, -4, 8, 1, 12)
                .setColors(new Color(255, 255, 255, 0).getRGB(),
                        new Color(238, 46, 46).getRGB(),
                        new Color(238, 116, 116).getRGB())
                .setIcon(GuiTextures.TERMINAL_DELETE)
                .setHoverText("delete")
                .setClickListener(this::delete));
        addWidget(toolButtons);
    }

    public void setGuideConfigEditor(GuideConfigEditor configEditor) {
        this.configEditor = configEditor;
    }

    private void setToolButton(int x, int y, int width, int height) {
        toolButtons.setVisible(true);
        toolButtons.setSelfPosition(new Position(x + width / 2, y));
    }

    private void delete(ClickData clickData) {
        removeWidget(selected);
        selected = null;
        configEditor.loadConfigurator(null, null, true);
        toolButtons.setSelfPosition(new Position(0, 0));
        toolButtons.setVisible(false);
    }

    private void moveUp(ClickData clickData) {
        moveUp(selected);
    }

    private void moveDown(ClickData clickData) {
        moveDown(selected);
    }

    public String getJsonString() {
        JsonObject json = new JsonObject();
        json.addProperty("section", "");
        json.addProperty("title", title.content.get(0));
        JsonArray array = new JsonArray();
        json.add("stream", array);
        stream.forEach(widget -> array.add(configMap.get(widget)));

        JsonArray array2 = new JsonArray();
        json.add("fixed", array2);
        fixed.forEach(widget -> array2.add(configMap.get(widget)));

        return new Gson().toJson(json);
    }

    public JsonObject addGuideWidget(IGuideWidget widget, boolean isFixed) {
        int pageWidth = this.getSize().width - 5;
        JsonObject widgetConfig = widget.getTemplate(isFixed);
        Widget guideWidget;
        if (isFixed) {
            int width = widgetConfig.get("width").getAsInt();
            int height = widgetConfig.get("height").getAsInt();

            guideWidget = widget.createFixedWidget(
                    (pageWidth - width) / 2 + 5,
                    this.scrollYOffset + (this.getSize().height - height) / 2,
                    width,
                    height,
                    widgetConfig);
            fixed.add(guideWidget);
            this.addWidget(guideWidget);
        } else {
            int y = getStreamBottom();
            guideWidget = widget.createStreamWidget(5, y + 5, pageWidth, widgetConfig);
            stream.add(guideWidget);
            this.addWidget(guideWidget);
        }
        configMap.put(guideWidget, widgetConfig);
        return widgetConfig;
    }


    public void moveUp(Widget widget) {
        int index = stream.indexOf(widget);
        if (index > 0) {
            Widget target = stream.get(index - 1);
            if (interpolator == null) {
                int offsetD = 5 + widget.getSize().height;
                int offsetU = widget.getPosition().y - target.getPosition().y;
                int y1 = widget.getSelfPosition().y;
                int y2 = target.getSelfPosition().y;
                interpolator = new Interpolator(0, 1, 10, Eases.EaseLinear, value->{
                    widget.setSelfPosition(new Position(widget.getSelfPosition().x, (int) (y1 - value.floatValue() * offsetU)));
                    target.setSelfPosition(new Position(target.getSelfPosition().x, (int) (y2 + value.floatValue() * offsetD)));
                    if (widget == selected) {
                        setToolButton(selected.getSelfPosition().x, selected.getSelfPosition().y, selected.getSize().width, selected.getSize().height);
                    }
                    widget.setVisible(widget.getSelfPosition().y < scrollYOffset + getSize().height && widget.getSelfPosition().y + widget.getSize().height > 0);
                    target.setVisible(target.getSelfPosition().y < scrollYOffset + getSize().height && target.getSelfPosition().y + target.getSize().height > 0);
                }, value->{
                    interpolator = null;
                    stream.remove(widget);
                    stream.add(index - 1, widget);
                }).start();
            }
        }
    }

    public void moveDown(Widget widget) {
        int index = stream.indexOf(widget);
        if (index >= 0 && index < stream.size() - 1) {
            Widget target = stream.get(index + 1);
            if (interpolator == null) {
                int offsetD = 5 + target.getSize().height;
                int offsetU = target.getPosition().y - widget.getPosition().y;
                int y1 = widget.getSelfPosition().y;
                int y2 = target.getSelfPosition().y;
                interpolator = new Interpolator(0, 1, 10, Eases.EaseLinear, value->{
                    widget.setSelfPosition(new Position(widget.getSelfPosition().x, (int) (y1 + value.floatValue() * offsetD)));
                    target.setSelfPosition(new Position(target.getSelfPosition().x, (int) (y2 - value.floatValue() * offsetU)));
                    if (widget == selected) {
                        setToolButton(selected.getSelfPosition().x, selected.getSelfPosition().y, selected.getSize().width, selected.getSize().height);
                    }
                    widget.setVisible(widget.getSelfPosition().y < getSize().height - xBarHeight && widget.getSelfPosition().y + widget.getSize().height > 0);
                    target.setVisible(target.getSelfPosition().y < getSize().height - xBarHeight && target.getSelfPosition().y + target.getSize().height > 0);
                }, value->{
                    interpolator = null;
                    stream.remove(widget);
                    stream.add(index + 1, widget);
                }).start();
            }
        }
    }

    @Override
    protected void setScrollYOffset(int scrollYOffset) {
        if (scrollYOffset == this.scrollYOffset) return;
        int offset = scrollYOffset - this.scrollYOffset;
        this.scrollYOffset = scrollYOffset;
        for (Widget widget : widgets) {
            Position newPos = widget.addSelfPosition(0, -offset);
            if (widget != toolButtons) {
                widget.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
            }
        }
    }

    @Override
    public void removeWidget(Widget widget) {
        int index = stream.indexOf(widget);
        if (index >= 0) {
            int offset = widget.getSize().height + 5;
            for (int i = stream.size() - 1; i > index; i--) {
                Widget bottom = stream.get(i);
                Position newPos = bottom.addSelfPosition(0, -offset);
                bottom.setVisible(newPos.y < getSize().height - xBarHeight && newPos.y + widget.getSize().height > 0);
            }
            stream.remove(widget);
        } else {
            fixed.remove(widget);
        }
        super.removeWidget(widget);
        configMap.remove(widget);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean flag = false;
        for (Widget widget : fixed) {
            if (widget.isMouseOverElement(mouseX, mouseY)) {
                if (widget instanceof IGuideWidget && widget != selected) {
                    configEditor.loadConfigurator((IGuideWidget) widget, configMap.get(widget), true);
                    selected = widget;
                    setToolButton(selected.getSelfPosition().x, selected.getSelfPosition().y, selected.getSize().width, selected.getSize().height);
                }
                playButtonClickSound();
                flag = true;
                break;
            }
        }
        for (Widget widget : stream) {
            if (widget.isMouseOverElement(mouseX, mouseY)) {
                if (widget instanceof IGuideWidget && widget != selected) {
                    configEditor.loadConfigurator((IGuideWidget) widget, configMap.get(widget), false);
                    selected = widget;
                    setToolButton(selected.getSelfPosition().x, selected.getSelfPosition().y, selected.getSize().width, selected.getSize().height);
                }
                playButtonClickSound();
                flag = true;
                break;
            }
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return flag;
    }

    @Override
    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        for (Widget widget : widgets) {
            if (widget != toolButtons && widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (widget != selected) {
                        Position pos = widget.getPosition();
                        Size s = widget.getSize();
                        if (stream.contains(widget)) {
                            drawSolidRect(position.x, pos.y, size.width - yBarWidth, s.height, 0x6f000000);
                        } else {
                            drawSolidRect(pos.x, pos.y, s.width, s.height, 0x6f000000);
                        }
                    }
                }
            }
        }
        if (selected != null) {
            Position pos = selected.getPosition();
            Size s = selected.getSize();
            if (stream.contains(selected)) {
                drawSolidRect(position.x, pos.y, size.width - yBarWidth, s.height, 0x6f0000ff);
            } else {
                drawSolidRect(pos.x, pos.y, s.width, s.height, 0x6f0000ff);
            }
        }
        if(toolButtons.isVisible()) {
            toolButtons.drawInBackground(mouseX, mouseY, partialTicks, context);
        }
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        return true;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (super.mouseDragged(mouseX, mouseY, button, timeDragged)) {
            setToolButton(selected.getSelfPosition().x, selected.getSelfPosition().y, selected.getSize().width, selected.getSize().height);
            return true;
        }
        return false;
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
        if(fixed.contains(widget) && widget instanceof IDraggable) {
            ((IDraggable) widget).setDraggable(true);
        }
    }
}
