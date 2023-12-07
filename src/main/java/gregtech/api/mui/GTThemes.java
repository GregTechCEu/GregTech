package gregtech.api.mui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.utils.JsonBuilder;

import gregtech.common.ConfigHolder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum GTThemes {

    /** Standard theme used by most GT UIs */
    STANDARD("gregtech_standard") {
        @Override
        protected void onReload() {
            builder.add("color", ConfigHolder.client.defaultUIColor);
            builder.add("panel", new JsonBuilder()
                    .add("background", new JsonBuilder()
                            .add("type", "texture")
                            .add("id", "gregtech_standard_bg")));
        }
    },
    /** Bronze-colored theme used by Bronze Steam machines */
    BRONZE("gregtech_bronze"),
    /** Steel-colored theme used by Steel Steam machines */
    STEEL("gregtech_steel"),
    /** Brown/Beige colored theme used by PBF, Coke Oven, etc. */
    PRIMITIVE("gregtech_primitive");

    private final String id;
    protected final JsonBuilder builder;

    public static final GTThemes[] VALUES = values();

    GTThemes(String id) {
        this.id = id;
        this.builder = new JsonBuilder();
    }

    public String getId() {
        return id;
    }

    private void register() {
        IThemeApi.get().registerTheme(id, builder);
    }

    protected void onReload() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTThemes.class);
        // todo try to link this BG to the theme in a better way
        GuiTextures.registerBackground("gregtech_standard_bg", GTGuiTextures.BACKGROUND);
        for (GTThemes theme : VALUES) {
            theme.register();
        }
    }

    @SubscribeEvent
    public static void onReloadThemes(ReloadThemeEvent.Pre event) {
        for (GTThemes theme : VALUES) {
            theme.onReload();
        }
    }
}
