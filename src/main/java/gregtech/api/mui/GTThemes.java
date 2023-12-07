package gregtech.api.mui;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.utils.JsonBuilder;

import gregtech.common.ConfigHolder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.Nullable;

public enum GTThemes {

    /** Standard theme used by most GT UIs */
    STANDARD("gregtech_standard",
            GTGuiTextures.IDs.STANDARD_BACKGROUND,
            null,
            null,
            null,
            ConfigHolder.client.defaultUIColor),

    /** Bronze-colored theme used by Bronze Steam machines */
    // todo
    BRONZE("gregtech_bronze",
            GTGuiTextures.IDs.BRONZE_BACKGROUND,
            null,
            null,
            null,
            -1),

    /** Steel-colored theme used by Steel Steam machines */
    // todo
    STEEL("gregtech_steel",
            GTGuiTextures.IDs.STEEL_BACKGROUND,
            null,
            null,
            null,
            -1),

    /** Brown/Beige colored theme used by PBF, Coke Oven, etc. */
    // todo
    PRIMITIVE("gregtech_primitive",
            GTGuiTextures.IDs.PRIMITIVE_BACKGROUND,
            null,
            null,
            null,
            -1);

    private final String id;
    private final String panel;
    private final String button;
    private final String itemSlot;
    private final String fluidSlot;
    private final int color;
    private final JsonBuilder builder;

    public static final GTThemes[] VALUES = values();

    GTThemes(String id, @Nullable String panel, @Nullable String button,
             @Nullable String itemSlot, @Nullable String fluidSlot, int color) {
        this.id = id;
        this.panel = panel;
        this.button = button;
        this.itemSlot = itemSlot;
        this.fluidSlot = fluidSlot;
        this.color = color;
        this.builder = new JsonBuilder();
    }

    public String getId() {
        return id;
    }

    private void register() {
        IThemeApi.get().registerTheme(id, builder);
    }

    private void onReload() {
        if (panel != null) {
            builder.add("panel", new JsonBuilder()
                    .add("background", new JsonBuilder()
                            .add("type", "texture")
                            .add("id", panel)));
        }
        if (button != null) {
            builder.add("button", new JsonBuilder()
                    .add("background", new JsonBuilder()
                            .add("type", "texture")
                            .add("id", button)));
        }
        if (itemSlot != null) {
            builder.add("itemSlot", new JsonBuilder()
                    .add("background", new JsonBuilder()
                            .add("type", "texture")
                            .add("id", button)));
        }
        if (fluidSlot != null) {
            builder.add("fluidSlot", new JsonBuilder()
                    .add("background", new JsonBuilder()
                            .add("type", "texture")
                            .add("id", button)));
        }
        if (color >= 0) {
            builder.add("color", color);
        }
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTThemes.class);
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
