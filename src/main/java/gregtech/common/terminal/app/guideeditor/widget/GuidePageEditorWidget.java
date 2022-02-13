package gregtech.common.terminal.app.guideeditor.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.common.terminal.app.guide.widget.GuidePageWidget;
import gregtech.common.terminal.app.guide.widget.IGuideWidget;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.CustomPositionSizeWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuidePageEditorWidget extends GuidePageWidget {
    private Widget selected;
    private final WidgetGroup toolButtons;
    private final CustomPositionSizeWidget customPositionSizeWidget;
    private GuideConfigEditor configEditor;
    private String section = "default";

    public GuidePageEditorWidget(int xPosition, int yPosition, int width, int height, int margin) {
        super(xPosition, yPosition, width, height, margin);
        this.setDraggable(false);
        setTitle("Template");
        customPositionSizeWidget = new CustomPositionSizeWidget(0xff0000ff, 0xffff0000, 2).setOnUpdated(this::onPosSizeChanged);
        toolButtons = new WidgetGroup(Position.ORIGIN, Size.ZERO);
        toolButtons.setVisible(false);
        toolButtons.addWidget(new CircleButtonWidget(-20, -4, 8, 1, 12)
                .setColors(0,
                        TerminalTheme.COLOR_B_2.getColor(),
                        TerminalTheme.COLOR_1.getColor())
                .setIcon(GuiTextures.ICON_UP)
                .setHoverText("terminal.guide_editor.up")
                .setClickListener(this::moveUp));
        toolButtons.addWidget(new CircleButtonWidget(0, -4, 8, 1, 12)
                .setColors(0,
                        TerminalTheme.COLOR_B_2.getColor(),
                        TerminalTheme.COLOR_2.getColor())
                .setIcon(GuiTextures.ICON_DOWN)
                .setHoverText("terminal.guide_editor.down")
                .setClickListener(this::moveDown));
        toolButtons.addWidget(new CircleButtonWidget(20, -4, 8, 1, 12)
                .setColors(0,
                        TerminalTheme.COLOR_B_2.getColor(),
                        TerminalTheme.COLOR_3.getColor())
                .setIcon(GuiTextures.ICON_REMOVE)
                .setHoverText("terminal.guide_editor.remove")
                .setClickListener(this::delete));
        addWidget(customPositionSizeWidget);
        addWidget(toolButtons);
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }

    private void onPosSizeChanged(Position pos, Size size) {
        Widget widget = customPositionSizeWidget.getControlled();
        if (widget instanceof IGuideWidget && ((IGuideWidget) widget).isFixed()) {
            JsonObject config = ((IGuideWidget) widget).getConfig();
            if (config.has("x")) {
                config.addProperty("x", pos.x + scrollXOffset);
                ((IGuideWidget) widget).updateValue("x");
            }
            if (config.has("y")) {
                config.addProperty("y", pos.y + scrollYOffset);
                ((IGuideWidget) widget).updateValue("y");
            }
            if (config.has("width")) {
                config.addProperty("width", size.width);
                ((IGuideWidget) widget).updateValue("width");
            }
            if (config.has("height")) {
                config.addProperty("height", size.height);
                ((IGuideWidget) widget).updateValue("height");
            }
            ((IGuideWidget) widget).onFixedPositionSizeChanged(pos, size);
        }
        toolButtons.setSelfPosition(new Position(pos.x + size.width / 2, pos.y));
    }

    public void setGuideConfigEditor(GuideConfigEditor configEditor) {
        this.configEditor = configEditor;
    }

    private void setToolButton(Widget widget) {
        customPositionSizeWidget.setControlled(widget);
        customPositionSizeWidget.setVisible(true);
        customPositionSizeWidget.setActive(!(widget instanceof IGuideWidget) || ((IGuideWidget) widget).isFixed());
        toolButtons.setVisible(true);
        toolButtons.setSelfPosition(new Position(widget.getSelfPosition().x + widget.getSize().width / 2, widget.getSelfPosition().y));
    }

    private void delete(ClickData clickData) {
        removeWidget(selected);
        selected = null;
        configEditor.loadConfigurator(null);
        toolButtons.setSelfPosition(new Position(-scrollYOffset, -scrollYOffset));
        customPositionSizeWidget.setControlled(null);
        toolButtons.setVisible(false);
    }

    private void moveUp(ClickData clickData) {
        moveUp(selected);
    }

    private void moveDown(ClickData clickData) {
        moveDown(selected);
    }

    public JsonObject getJsonConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("section", section);
        json.addProperty("title", title.content.get(0));
        JsonArray array = new JsonArray();
        json.add("stream", array);
        stream.forEach(widget -> {
            if (widget instanceof IGuideWidget) {
                array.add(((IGuideWidget) widget).getConfig());
            }
        });

        JsonArray array2 = new JsonArray();
        json.add("fixed", array2);
        fixed.forEach(widget -> {
            if (widget instanceof IGuideWidget) {
                array2.add(((IGuideWidget) widget).getConfig());
            }
        });

        return json;
    }

    public JsonObject addGuideWidget(IGuideWidget widget, boolean isFixed) {
        int pageWidth = getPageWidth();
        int margin = getMargin();
        JsonObject widgetConfig = widget.getTemplate(isFixed);
        Widget guideWidget;
        if (isFixed) {
            int width = widgetConfig.get("width").getAsInt();
            int height = widgetConfig.get("height").getAsInt();

            guideWidget = widget.updateOrCreateFixedWidget(
                    (pageWidth - width) / 2 + 5,
                    this.scrollYOffset + (this.getSize().height - height) / 2,
                    width,
                    height,
                    widgetConfig);
            fixed.add(guideWidget);
            this.addWidget(guideWidget);
        } else {
            int index = stream.indexOf(selected);
            if (index >= 0) {
                guideWidget = widget.updateOrCreateStreamWidget(margin,
                        selected.getSize().height + selected.getSelfPosition().y + scrollYOffset + 5,
                        pageWidth - 2 * margin, widgetConfig);
                for (int i = index + 1; i < stream.size(); i++) {
                    stream.get(i).addSelfPosition(0, guideWidget.getSize().height + 5);
                }
                stream.add(index + 1, guideWidget);
            } else {
                guideWidget = widget.updateOrCreateStreamWidget(margin, getStreamBottom() + 5, pageWidth - 2 * margin, widgetConfig);
                stream.add(guideWidget);
            }
            this.addWidget(guideWidget);
            computeMax();
        }
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
                        setToolButton(selected);
                    }
                    widget.setVisible(widget.getSelfPosition().y < scrollYOffset + getSize().height && widget.getSelfPosition().y + widget.getSize().height > 0);
                    target.setVisible(target.getSelfPosition().y < scrollYOffset + getSize().height && target.getSelfPosition().y + target.getSize().height > 0);
                }, value->{
                    interpolator = null;
                    stream.remove(widget);
                    stream.add(index - 1, widget);
                }).start();
            }
        } else {
            int index2 = fixed.indexOf(widget);
            if (index2 >= 0 && index2 < fixed.size() - 1) {
                Widget target = fixed.get(index2 + 1);
                fixed.remove(widget);
                fixed.add(index2 + 1, widget);
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
                        setToolButton(selected);
                    }
                    widget.setVisible(widget.getSelfPosition().y < getSize().height - xBarHeight && widget.getSelfPosition().y + widget.getSize().height > 0);
                    target.setVisible(target.getSelfPosition().y < getSize().height - xBarHeight && target.getSelfPosition().y + target.getSize().height > 0);
                }, value->{
                    interpolator = null;
                    stream.remove(widget);
                    stream.add(index + 1, widget);
                }).start();
            }
        } else {
            int index2 = fixed.indexOf(widget);
            if (index2 > 0) {
                Widget target = fixed.get(index2 - 1);
                fixed.remove(widget);
                fixed.add(index2 - 1, widget);
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
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        boolean flag = false;
        for (int i = fixed.size() - 1; i >= 0; i--) {
            Widget widget = fixed.get(i);
            if (widget.isMouseOverElement(mouseX, mouseY)) {
                if (widget instanceof IGuideWidget && widget != selected) {
                    configEditor.loadConfigurator((IGuideWidget) widget);
                    selected = widget;
                    setToolButton(selected);
                }
                playButtonClickSound();
                flag = true;
                break;
            }
        }
        if (!flag) {
            for (Widget widget : stream) {
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (widget instanceof IGuideWidget && widget != selected) {
                        configEditor.loadConfigurator((IGuideWidget) widget);
                        selected = widget;
                        setToolButton(selected);
                    }
                    playButtonClickSound();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    @Override
    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int width = getSize().width;
        if(title.isVisible()) {
            title.drawInBackground(mouseX, mouseY, partialTicks, context);
        }
        for (Widget widget : stream) {
            if (widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);
            }
        }

        boolean flag = false;
        for (Widget widget : fixed) {
            if (widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (widget != selected) {
                        drawSelectedBorder(x, width, widget);
                    }
                    flag = true;
                }
            }
        }
        if (!flag) {
            for (Widget widget : stream) {
                if (widget.isVisible() && widget != selected && widget.isMouseOverElement(mouseX, mouseY)) {
                    drawSelectedBorder(x, width, widget);
                }
            }
        }

        if (selected != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int index = fixed.indexOf(selected);
            String layer = "L: " + (index >= 0 ? index : stream.indexOf(selected));
            fontRenderer.drawString(layer,
                    selected.getPosition().x + (selected.getSize().width - fontRenderer.getStringWidth(layer)) / 2,
                    selected.getPosition().y - 20,
                    0xffff0000, true);
        }
        if(toolButtons.isVisible()) {
            customPositionSizeWidget.drawInBackground(mouseX, mouseY, partialTicks, context);
            toolButtons.drawInBackground(mouseX, mouseY, partialTicks, context);
        }
        return true;
    }

    private void drawSelectedBorder(int x, int width, Widget widget) {
        Position pos = widget.getPosition();
        Size s = widget.getSize();
        if (stream.contains(widget)) {
            drawSolidRect(x, pos.y, width - yBarWidth, s.height, 0x6f000000);

        } else {
            drawSolidRect(pos.x, pos.y, s.width, s.height, 0x6f000000);
        }
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (super.mouseDragged(mouseX, mouseY, button, timeDragged) && toolButtons.isVisible()) {
            setToolButton(selected);
            return true;
        }
        return false;
    }

    @Override
    public void clearAllWidgets() {
        super.clearAllWidgets();
        selected = null;
        configEditor.loadConfigurator(null);
        toolButtons.setSelfPosition(new Position(-scrollYOffset, -scrollYOffset));
        customPositionSizeWidget.setControlled(null);
        toolButtons.setVisible(false);
        addWidget(customPositionSizeWidget);
        addWidget(toolButtons);
    }
}
