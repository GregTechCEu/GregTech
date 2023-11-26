package gregtech.common.terminal.app.guide.widget;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuidePageWidget extends DraggableScrollableWidgetGroup {

    public static final Map<String, IGuideWidget> REGISTER_WIDGETS = new HashMap<>();

    static { // register guide widgets
        REGISTER_WIDGETS.put(TextBoxWidget.NAME, new TextBoxWidget());
        REGISTER_WIDGETS.put(ImageWidget.NAME, new ImageWidget());
        REGISTER_WIDGETS.put(CardWidget.NAME, new CardWidget());
        REGISTER_WIDGETS.put(SlotListWidget.NAME, new SlotListWidget());
        REGISTER_WIDGETS.put(TankListWidget.NAME, new TankListWidget());
    }

    protected TextBoxWidget title;
    protected List<Widget> stream = new ArrayList<>();
    protected List<Widget> fixed = new ArrayList<>();
    protected Interpolator interpolator;
    private final int margin;

    public GuidePageWidget(int xPosition, int yPosition, int width, int height, int margin) {
        super(xPosition, yPosition, width, height);
        this.margin = margin;
        this.setBackground(new ColorRectTexture(-1))
                .setDraggable(true)
                .setYScrollBarWidth(4)
                .setYBarStyle(new ColorRectTexture(new Color(142, 142, 142)),
                        new ColorRectTexture(new Color(148, 226, 193)));
        this.setUseScissor(false);
    }

    public int getPageWidth() {
        return this.getSize().width - yBarWidth;
    }

    public int getMargin() {
        return margin;
    }

    public void setTitle(String config) {
        int x = 5;
        int y = 2;
        int width = this.getSize().width - yBarWidth - 10;
        int height = 0;
        if (title != null) {
            height = title.getSize().height;
            x = title.getSelfPosition().x;
            y = title.getSelfPosition().y;
            removeWidget(title);
        }
        title = new TextBoxWidget(5, 2, width,
                Collections.singletonList(config),
                0, 15, 0xffffffff, 0x6fff0000, 0xff000000,
                true, true);
        this.addWidget(title);
        title.setSelfPosition(new Position(x, y));
        int offset = title.getSize().height - height;
        if (offset != 0) {
            for (Widget widget : stream) {
                widget.addSelfPosition(0, offset);
            }
        }
    }

    public String getTitle() {
        return title == null ? "" : String.join("\n", title.content);
    }

    public String loadJsonConfig(String config) {
        try {
            loadJsonConfig(new JsonParser().parse(config).getAsJsonObject());
        } catch (Exception e) {
            this.clearAllWidgets();
            return e.getMessage();
        }
        return null;
    }

    public void loadJsonConfig(JsonObject config) {
        this.stream.clear();
        this.fixed.clear();
        this.title = null;
        this.clearAllWidgets();
        int pageWidth = getPageWidth();
        int margin = getMargin();
        // add title
        setTitle(config.get("title").getAsString());

        // add stream widgets
        if (config.has("stream")) {
            stream = new ArrayList<>();
            int y = title.getSize().height + 10;
            for (JsonElement element : config.getAsJsonArray("stream")) {
                JsonObject widgetConfig = element.getAsJsonObject();
                Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString())
                        .updateOrCreateStreamWidget(margin, y, pageWidth - 2 * margin, widgetConfig);
                y += widget.getSize().height + 5;
                stream.add(widget);
                this.addWidget(widget);
            }
        }
        // add fixed widgets
        if (config.has("fixed")) {
            fixed = new ArrayList<>();
            for (JsonElement element : config.getAsJsonArray("fixed")) {
                JsonObject widgetConfig = element.getAsJsonObject();
                Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString()).updateOrCreateFixedWidget(
                        widgetConfig.get("x").getAsInt(),
                        widgetConfig.get("y").getAsInt(),
                        widgetConfig.get("width").getAsInt(),
                        widgetConfig.get("height").getAsInt(),
                        widgetConfig);
                fixed.add(widget);
                this.addWidget(widget);
            }
        }
    }

    public void onSizeUpdate(Widget widget, Size oldSize) {
        int offset = widget.getSize().height - oldSize.height;
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getSelfPosition().y);
        int index = stream.indexOf(widget);
        if (index < 0) return;
        for (int i = stream.size() - 1; i > index; i--) {
            Widget nextWidget = stream.get(i);
            nextWidget.addSelfPosition(0, offset);
        }
    }

    public void onPositionUpdate(Widget widget, Position oldPosition) {
        if (oldPosition.y + widget.getSize().height == maxHeight) {
            maxHeight = 0;
            for (Widget widget1 : widgets) {
                maxHeight = Math.max(maxHeight, widget1.getSize().height + widget1.getSelfPosition().y + scrollYOffset);
            }
        }
    }

    protected int getStreamBottom() {
        if (stream != null && stream.size() > 0) {
            Widget widget = stream.get(stream.size() - 1);
            return widget.getSize().height + widget.getSelfPosition().y + scrollYOffset;
        } else {
            return title.getSize().height + 10;
        }
    }

    @Override
    public void updateScreen() {
        if (interpolator != null) interpolator.update();
        super.updateScreen();
    }

    public void jumpToRef(String ref) {
        if (interpolator != null && !interpolator.isFinish()) return;
        for (Widget widget : widgets) {
            if (widget instanceof IGuideWidget && ref.equals(((IGuideWidget) widget).getRef())) {
                interpolator = new Interpolator(scrollYOffset, widget.getSelfPosition().y + scrollYOffset, 20,
                        Eases.QUAD_OUT,
                        value -> setScrollYOffset(value.intValue()),
                        value -> interpolator = null);
                interpolator.start();
            }
        }
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
        if (widget instanceof IGuideWidget) {
            ((IGuideWidget) widget).setPage(this);
        }
    }
}
