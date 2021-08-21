package gregtech.common.terminal.app.guide.widget;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.common.terminal.app.guideeditor.widget.configurator.ColorConfigurator;
import gregtech.common.terminal.app.guideeditor.widget.configurator.NumberConfigurator;
import gregtech.common.terminal.app.guideeditor.widget.configurator.StringConfigurator;
import gregtech.common.terminal.app.guideeditor.widget.configurator.TextListConfigurator;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class GuideWidgetGroup extends WidgetGroup implements IGuideWidget {
    //config
    public String ref;
    public int fill;
    public int stroke;
    public int stroke_width = 1;
    public String link;
    public List<String> hover_text;

    private transient boolean isFixed;
    protected transient GuidePageWidget page;
    protected transient JsonObject config;

    public GuideWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public GuideWidgetGroup(){
        super(Position.ORIGIN, Size.ZERO);
    }

    public abstract String getRegistryName();

    @Override
    public JsonObject getConfig() {
        return config;
    }

    @Override
    public boolean isFixed() {
        return isFixed;
    }

    @Override
    public void setStroke(int color) {
        this.stroke = color;
    }

    @Override
    public void setSize(Size size) {
        Size oldSize = this.getSize();
        super.setSize(size);
        if (page != null) {
            page.onSizeUpdate(this, oldSize);
        }
    }

    @Override
    protected void recomputePosition() {
        Position oldPosition = getPosition();
        super.recomputePosition();
        if (page != null) {
            page.onPositionUpdate(this, oldPosition);
        }
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = new JsonObject();
        template.addProperty("type", getRegistryName());
        if (isFixed) {
            template.addProperty("x", 0);
            template.addProperty("y", 0);
            template.addProperty("width", 100);
            template.addProperty("height", 100);
        }
        template.addProperty("ref", (String) null);
        template.addProperty("stroke", (String) null);
        template.addProperty("stroke_width", (String) null);
        template.addProperty("fill", (String) null);
        template.addProperty("link", (String) null);
        template.add("hover_text", new Gson().toJsonTree(hover_text));
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        group.addWidget(new ColorConfigurator(group, config, "fill", 0).setOnUpdated(needUpdate));
        group.addWidget(new ColorConfigurator(group, config, "stroke", 0).setOnUpdated(needUpdate));
        group.addWidget(new NumberConfigurator(group, config, "stroke_width", 1).setOnUpdated(needUpdate));
        group.addWidget(new StringConfigurator(group, config, "ref", "").setOnUpdated(needUpdate));
        group.addWidget(new StringConfigurator(group, config, "link", "").setOnUpdated(needUpdate));
        group.addWidget(new TextListConfigurator(group, 40, config, "hover_text", "").setOnUpdated(needUpdate));
    }

    @Override
    public String getRef() {
        return ref;
    }

    protected Widget initStream() {
        return initFixed();
    }

    protected abstract Widget initFixed();

    @Override
    public Widget updateOrCreateStreamWidget(int x, int y, int pageWidth, JsonObject config) {
        if (config == null) {
            return initStream();
        }
        GuideWidgetGroup widget = new Gson().fromJson(config, this.getClass());
        widget.isFixed = false;
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(pageWidth, 0));
        widget.config = config;
        return widget.initStream();
    }

    @Override
    public Widget updateOrCreateFixedWidget(int x, int y, int width, int height, JsonObject config) {
        if (config == null) {
            return initFixed();
        }
        GuideWidgetGroup widget = new Gson().fromJson(config, this.getClass());
        widget.isFixed = true;
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(width, height));
        widget.config = config;
        return widget.initFixed();
    }

    @Override
    public void setPage(GuidePageWidget page) {
        this.page = page;
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
        if (widget instanceof IGuideWidget) {
            ((IGuideWidget) widget).setPage(page);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (link != null && isMouseOverElement(mouseX, mouseY)) {
            Position position = getPosition();
            Size size = getSize();
            drawBorder(position.x, position.y, size.width, size.height, 0xff0000ff, stroke_width);
        }
        if ((hover_text != null || link != null) && isMouseOverElement(mouseX, mouseY)) {
            List<String> tooltip = hover_text == null ? new ArrayList<>() : new ArrayList<>(hover_text);
            if (link != null) {
                tooltip.add("§9Ctrl+Click§r §e(" + link + ")§r");
            }
            drawHoveringText(ItemStack.EMPTY, tooltip, 100, mouseX, mouseY);
        }
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if(stroke != 0) {
            drawBorder(position.x, position.y, size.width, size.height, stroke, stroke_width);
        }
        if (fill != 0) {
            drawSolidRect(position.x, position.y, size.width, size.height, fill);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (link != null && isMouseOverElement(mouseX, mouseY) && isCtrlDown()) {
            page.jumpToRef(link);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
