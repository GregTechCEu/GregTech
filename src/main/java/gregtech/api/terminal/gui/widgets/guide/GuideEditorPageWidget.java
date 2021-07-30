package gregtech.api.terminal.gui.widgets.guide;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;

public class GuideEditorPageWidget extends GuidePageWidget {
    private final BiMap<Widget, JsonObject> configMap;

    public GuideEditorPageWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
        configMap =  HashBiMap.create();
        setTitle("Template");
    }

    public String getJsonString() {
        JsonObject json = new JsonObject();
        json.addProperty("section", "");
        json.addProperty("title", title.content.get(0));
        if (stream != null) {
            JsonArray array = new JsonArray();
            json.add("stream", array);
            stream.forEach(widget -> array.add(configMap.get(widget)));
        }
        if (fixed != null) {
            JsonArray array = new JsonArray();
            json.add("fixed", array);
            fixed.forEach(widget -> array.add(configMap.get(widget)));
        }
        return new Gson().toJson(json);
    }

    public JsonObject addGuideWidget(IGuideWidget widget, boolean isFixed) {
        int pageWidth = this.getSize().width;
        JsonObject widgetConfig = widget.getTemplate(isFixed);
        Widget guideWidget;
        if (isFixed) {
            guideWidget = widget.createFixedWidget(widgetConfig.get("x").getAsInt(),
                    widgetConfig.get("y").getAsInt(),
                    widgetConfig.get("width").getAsInt(),
                    widgetConfig.get("height").getAsInt(),
                    widgetConfig);
            this.addWidget(guideWidget);
            fixed.add(guideWidget);
        } else {
            int y = getStreamBottom();
            guideWidget = widget.createStreamWidget(5, y + 5, pageWidth - 5, widgetConfig);
            this.addWidget(guideWidget);
            stream.add(guideWidget);
        }
        configMap.put(guideWidget, widgetConfig);
        return widgetConfig;
    }


    public void moveUp(Widget widget) {
        int index = stream.indexOf(widget);
        if (index > 0) {
            Widget target = stream.get(index - 1);
            if (interpolator == null) {
                int offset = widget.getPosition().y - target.getPosition().y;
                int y1 = widget.getSelfPosition().y;
                int y2 = target.getSelfPosition().y;
                interpolator = new Interpolator(0, offset, 10, Eases.EaseLinear, value->{
                    widget.setSelfPosition(new Position(widget.getSelfPosition().x, y1 - value.intValue()));
                    target.setSelfPosition(new Position(target.getSelfPosition().x, y2 + value.intValue()));
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
                int offset = target.getPosition().y - widget.getPosition().y;
                int y1 = widget.getSelfPosition().y;
                int y2 = target.getSelfPosition().y;
                interpolator = new Interpolator(0, offset, 10, Eases.EaseLinear, value->{
                    widget.setSelfPosition(new Position(widget.getSelfPosition().x, y1 + value.intValue()));
                    target.setSelfPosition(new Position(target.getSelfPosition().x, y2 - value.intValue()));
                }, value->{
                    interpolator = null;
                    stream.remove(widget);
                    stream.add(index + 1, widget);
                }).start();
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
                bottom.setSelfPosition(new Position(bottom.getSelfPosition().x, bottom.getSelfPosition().y - offset));
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }
}
