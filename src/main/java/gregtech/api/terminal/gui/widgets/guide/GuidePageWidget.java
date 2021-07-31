package gregtech.api.terminal.gui.widgets.guide;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.terminal.gui.widgets.ScrollablePanelWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GuidePageWidget extends ScrollablePanelWidgetGroup {
    public static final Map<String, IGuideWidget> REGISTER_WIDGETS = new HashMap<>();
    static { //register guide widgets
        REGISTER_WIDGETS.put("textbox", new TextBoxWidget());
        REGISTER_WIDGETS.put("image", new ImageWidget());
    }
    protected TextBoxWidget title;
    protected List<Widget> stream;
    protected List<Widget> fixed;

    protected Interpolator interpolator;

    public GuidePageWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
        this.setBackground(new ColorRectTexture(-1));
        this.setDraggable(true);
        this.availableYScrollBarWidth(4);
        this.setYBarStyle(new ColorRectTexture(new Color(142, 142, 142).getRGB())
                , new ColorRectTexture(new Color(148, 226, 193).getRGB()));
    }

    public void setTitle(String config) {
        int height = 0;
        if (title != null) {
            height = title.getSize().height;
            removeWidget(title);
        }
        title = new TextBoxWidget(5, 2, this.getSize().width - yBarWidth - 10,
                Collections.singletonList(config),
                0, 15, 0xffffffff, 0x6fff0000, 0xff000000,
                true, true);
        this.addWidget(title);
        if (stream != null) {
            int offset = title.getSize().height - height;
            if (offset != 0) {
                for (Widget widget : stream) {
                    widget.setSelfPosition(new Position(widget.getSelfPosition().x, widget.getSelfPosition().y + offset));
                }
            }
        }
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
        int pageWidth = this.getSize().width - yBarWidth;
        // add title
        setTitle(config.get("title").getAsString());

        // add stream widgets
        if (config.has("stream")) {
            stream = new ArrayList<>();
            int y = title.getSize().height + 10;
            for (JsonElement element : config.getAsJsonArray("stream")) {
                JsonObject widgetConfig = element.getAsJsonObject();
                Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString()).createStreamWidget(5, y, pageWidth - 10, widgetConfig);
                y += widget.getSize().height + 5;
                this.addWidget(widget);
                stream.add(widget);
            }
        }
        // add fixed widgets
        if (config.has("fixed")) {
            fixed = new ArrayList<>();
            for (JsonElement element : config.getAsJsonArray("fixed")) {
                JsonObject widgetConfig = element.getAsJsonObject();
                Widget widget = REGISTER_WIDGETS.get(widgetConfig.get("type").getAsString()).createFixedWidget(
                        widgetConfig.get("x").getAsInt(),
                        widgetConfig.get("y").getAsInt(),
                        widgetConfig.get("width").getAsInt(),
                        widgetConfig.get("height").getAsInt(),
                        widgetConfig);
                this.addWidget(widget);
                fixed.add(widget);
            }
        }
    }

    public void onSizeUpdate(Widget widget, Size oldSize) {
        int offset = widget.getSize().height - oldSize.height;
        maxHeight = Math.max(maxHeight, widget.getSize().height + widget.getPosition().y);
        if (stream != null) {
            int index = stream.indexOf(widget);
            for (int i = stream.size() - 1; i > index; i--) {
                Widget nextWidget = stream.get(i);
                nextWidget.setSelfPosition(new Position(nextWidget.getSelfPosition().x, nextWidget.getSelfPosition().y + offset));
            }
        }
    }

    public void onPositionUpdate(Widget widget, Position oldPosition) {
        if (oldPosition.y + widget.getSize().height == maxHeight) {
            maxHeight = 0;
            for (Widget widget1 : widgets) {
                maxHeight = Math.max(maxHeight, widget1.getSize().height + widget1.getPosition().y);
            }
        }
    }

    protected int getStreamBottom() {
        if (stream!= null && stream.size() > 0) {
            Widget widget = stream.get(stream.size() - 1);
            return widget.getSize().height + widget.getPosition().y;
        } else {
            return title.getSize().height + 10;
        }
    }

    @Override
    public void updateScreen() {
        if (interpolator != null) interpolator.update();
        super.updateScreen();
    }

    public void jumpToRef(String ref){
        if (interpolator != null && !interpolator.isFinish()) return;
        for (Widget widget : widgets) {
            if (widget instanceof IGuideWidget && ref.equals(((IGuideWidget) widget).getRef())) {
                interpolator = new Interpolator(scrollYOffset, widget.getSelfPosition().y, 20, Eases.EaseQuadOut,
                        value-> setScrollYOffset(value.intValue()),
                        value-> interpolator = null);
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
