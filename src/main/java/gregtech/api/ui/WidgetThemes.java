package gregtech.api.ui;

import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.theme.Theme;
import com.cleanroommc.modularui.theme.ThemeAPI;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.JsonBuilder;
import com.google.gson.JsonObject;
import gregtech.common.ConfigHolder;

import static gregtech.api.ui.UITextures.GT_BACKGROUND;
import static gregtech.api.ui.UITextures.GT_DISPLAY;

public final class WidgetThemes {

    public static final String GREGTECH_THEME = "gregtech_theme";

    public static void init() {
        ThemeAPI.INSTANCE.registerTheme(GREGTECH_THEME, new JsonBuilder()
                .add("color", String.valueOf(ConfigHolder.client.defaultUIColor)) // this has global scope
                .add(Theme.PANEL, new JsonBuilder() // this has widget type PANEL scope
                        .add("background", new JsonBuilder()
                                .add("type", "texture")
                                .add("name", GT_BACKGROUND)
                                .getJson())
                        .getJson())
//                .add(Theme.BUTTON, new JsonBuilder()
//                        .getJson())
//                .add(Theme.ITEM_SLOT, new JsonBuilder()
//                        .getJson())
//                .add(Theme.FLUID_SLOT, new JsonBuilder()
//                        .getJson())
                .add(Theme.TEXT_FIELD, new JsonBuilder() //TODO may not yet be functional
                        .add("background", new JsonBuilder()
                                .add("type", "texture")
                                .add("name", GT_DISPLAY)
                                .getJson())
                        .getJson())
        );
    }

    /**
     * Available fields are in the body of this constructor: {@link WidgetTheme#WidgetTheme(WidgetTheme, JsonObject, JsonObject)}
     * <p>
     * These fields can have global or widget-type scoping.
     * Global scoping is done in the main json object.
     * Widget-type scoping is done in a json object for the specific widget type.
     * <p>
     * See {@link Theme} for different categories of widgets that can have themes applied to them.
     * Theme string keys are the selector for the widget-type scope.
     * <p>
     * IDrawable json formats are found here: {@link DrawableSerialization#init()}
     * Textures defined in code can be used with {@code "type", "texture"} and {@code "name", REGISTERED_TEXTURE_NAME}
     * See {@link UITextures} for texture registration.
     */
    private static void docs() {}

    private WidgetThemes() {}
}
