package gregtech.common.terminal.app.guide.widget;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public interface IGuideWidget {
    String getRegistryName();

    JsonObject getConfig();

    boolean isFixed();

    Widget updateOrCreateStreamWidget(int x, int y, int pageWidth, JsonObject config);

    Widget updateOrCreateFixedWidget(int x, int y, int width, int height, JsonObject config);

    void setPage(GuidePageWidget page);

    default void updateValue(String field){
        JsonObject config = getConfig();
        if (config != null && config.has(field)) {
            try {
                Field f = this.getClass().getField(field);
                JsonElement value = config.get(field);
                if (value.isJsonNull()) {  // default
                    f.set(this, f.get(GuidePageWidget.REGISTER_WIDGETS.get(getRegistryName())));
                } else {
                    f.set(this, new Gson().fromJson(value, f.getGenericType()));
                }
                if (isFixed()) {
                    updateOrCreateFixedWidget(0,0,0,0,null);
                } else {
                    updateOrCreateStreamWidget(0,0,0,null);
                }
            } catch (Exception e) {
            }
        }
    }

    String getRef();

    JsonObject getTemplate(boolean isFixed);

    void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate);

    void setStroke(int color);

    default void onFixedPositionSizeChanged(Position position, Size size) {
        updateOrCreateFixedWidget(0,0,0,0,null);
    }
}
