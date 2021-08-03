package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.guide.configurator.ColorConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.NumberConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.StringConfigurator;
import gregtech.api.terminal.gui.widgets.guide.configurator.TextListConfigurator;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class GuideWidget extends Widget implements IGuideWidget {
    //config
    public String ref;
    public int fill;
    public int stroke;
    public int stroke_width = 1;
    public String link;
    public List<String> hover_text;

    private static final Gson GSON = new Gson();
    private transient boolean isFixed;
    protected transient GuidePageWidget page;

    public GuideWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public GuideWidget(){
        super(Position.ORIGIN, Size.ZERO);
    }

    public abstract String getRegistryName();

    public void updateValue(String field, JsonElement value) {
        try {
            Field f = this.getClass().getField(field);
            if (value.isJsonNull()) {  // default
                f.set(this, f.get(GuidePageWidget.REGISTER_WIDGETS.get(getRegistryName())));
            } else {
                f.set(this, new Gson().fromJson(value, f.getType()));
            }
            if (isFixed) {
                initFixed();
            } else {
                initStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFixed() {
        return isFixed;
    }

    @Override
    public void onFixedPositionSizeChanged(Position position, Size size) {
        this.initFixed();
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
    public void setSelfPosition(Position selfPosition) {
        super.setSelfPosition(selfPosition);
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = new JsonObject();
        if (isFixed) {
            template.addProperty("x", 0);
            template.addProperty("y", 0);
            template.addProperty("width", 100);
            template.addProperty("height", 100);
        }
        template.addProperty("ref", ref);
        template.addProperty("stroke", stroke);
        template.addProperty("stroke_width", stroke_width);
        template.addProperty("fill", fill);
        template.addProperty("link", link);
        template.add("hover_text", new Gson().toJsonTree(hover_text));
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        group.addWidget(new ColorConfigurator(5, group.getWidgetBottomHeight() + 5, config, "fill", 0).setOnUpdated(needUpdate));
        group.addWidget(new ColorConfigurator(5, group.getWidgetBottomHeight() + 5, config, "stroke", 0).setOnUpdated(needUpdate));
        group.addWidget(new NumberConfigurator(5, group.getWidgetBottomHeight() + 5, config, "stroke_width", 1).setOnUpdated(needUpdate));
        group.addWidget(new StringConfigurator(5, group.getWidgetBottomHeight() + 5, config, "ref", "").setOnUpdated(needUpdate));
        group.addWidget(new StringConfigurator(5, group.getWidgetBottomHeight() + 5, config, "link", "").setOnUpdated(needUpdate));
        group.addWidget(new TextListConfigurator(5, group.getWidgetBottomHeight() + 5, 40, config, "hover_text", true).setOnUpdated(needUpdate));
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public Widget createStreamWidget(int x, int y, int pageWidth, JsonObject config) {
        GuideWidget widget = GSON.fromJson(config, this.getClass());
        widget.isFixed = false;
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(pageWidth, 0));
        return widget.initStream();
    }

    @Override
    public Widget createFixedWidget(int x, int y, int width, int height, JsonObject config) {
        GuideWidget widget = GSON.fromJson(config, this.getClass());
        widget.isFixed = true;
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(width, height));
        return widget.initFixed();
    }

    protected Widget initStream() {
        return initFixed();
    }

    protected Widget initFixed() {
        return this;
    }

    @Override
    public void setPage(GuidePageWidget page) {
        this.page = page;
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
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if(stroke != 0) {
            drawBorder(position.x, position.y, size.width, size.height, stroke, stroke_width);
        }
        if (fill != 0) {
            drawGradientRect(position.x, position.y, size.width, size.height, fill, fill);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (link != null && isMouseOverElement(mouseX, mouseY) && isCtrlDown()) {
           page.jumpToRef(link);
           return true;
        }
        return false;
    }
}
