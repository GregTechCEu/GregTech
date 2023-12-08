package gregtech.api.mui;

import gregtech.common.ConfigHolder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.theme.ReloadThemeEvent;
import com.cleanroommc.modularui.utils.JsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GTGuiTheme {

    private static final List<GTGuiTheme> THEMES = new ArrayList<>();

    public static final GTGuiTheme STANDARD = new Builder("gregtech_standard")
            .panel(GTGuiTextures.IDs.STANDARD_BACKGROUND)
            .color(ConfigHolder.client.defaultUIColor)
            .build();

    public static final GTGuiTheme BRONZE = new Builder("gregtech_bronze")
            .panel(GTGuiTextures.IDs.BRONZE_BACKGROUND)
            .itemSlot(GTGuiTextures.IDs.BRONZE_SLOT)
            .build();

    public static final GTGuiTheme STEEL = new Builder("gregtech_steel")
            .panel(GTGuiTextures.IDs.STEEL_BACKGROUND)
            .itemSlot(GTGuiTextures.IDs.STEEL_SLOT)
            .build();

    public static final GTGuiTheme PRIMITIVE = new Builder("gregtech_primitive")
            .panel(GTGuiTextures.IDs.PRIMITIVE_BACKGROUND)
            .itemSlot(GTGuiTextures.IDs.PRIMITIVE_SLOT)
            .build();

    private final String themeId;

    private final List<Consumer<JsonBuilder>> elementBuilder;
    private final JsonBuilder jsonBuilder;

    private GTGuiTheme(String themeId) {
        this.themeId = themeId;
        this.jsonBuilder = new JsonBuilder();
        this.elementBuilder = new ArrayList<>();
        THEMES.add(this);
    }

    public String getId() {
        return themeId;
    }

    private void register() {
        buildJson();
        IThemeApi.get().registerTheme(themeId, jsonBuilder);
    }

    private void buildJson() {
        elementBuilder.forEach(c -> c.accept(jsonBuilder));
    }

    public static void registerThemes() {
        MinecraftForge.EVENT_BUS.register(GTGuiTheme.class);
        THEMES.forEach(GTGuiTheme::register);
    }

    @SubscribeEvent
    public static void onReloadThemes(ReloadThemeEvent.Pre event) {
        THEMES.forEach(GTGuiTheme::buildJson);
    }

    public static class Builder {

        private final GTGuiTheme theme;

        public Builder(String themeId) {
            theme = new GTGuiTheme(themeId);
        }

        /**
         * Set a parent theme for this theme, which unset values will inherit from.
         * If not set, it will use the default theme as the parent (VANILLA).
         */
        public Builder parent(String parentId) {
            theme.elementBuilder.add(b -> b.add("parent", parentId));
            return this;
        }

        /**
         * Set a background fallback for when specific widgets do not set their own.
         */
        public Builder globalBackground(String backgroundId) {
            theme.elementBuilder.add(b -> b.add("background", backgroundId));
            return this;
        }

        /**
         * Set a tooltip hover background fallback for when specific widgets do not set their own.
         */
        public Builder globalHoverBackground(String hoverBackgroundId) {
            theme.elementBuilder.add(b -> b.add("hoverBackground", hoverBackgroundId));
            return this;
        }

        /** Set a global UI coloration for this theme. */
        public Builder color(int color) {
            theme.elementBuilder.add(b -> b.add("color", color));
            return this;
        }

        /** Set a global UI text coloration for this theme. */
        public Builder textColor(int textColor) {
            theme.elementBuilder.add(b -> b.add("textColor", textColor));
            return this;
        }

        /** Enable text shadow for the global UI text for this theme. */
        public Builder textShadow() {
            theme.elementBuilder.add(b -> b.add("textShadow", true));
            return this;
        }

        /**
         * Set a custom panel (background texture) for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerBackground}!
         */
        public Builder panel(String panelId) {
            theme.elementBuilder.add(b -> b
                    .add("panel", new JsonBuilder()
                            .add("background", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", panelId))));
            return this;
        }

        /**
         * Set a custom button texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         */
        public Builder button(String buttonId) {
            return button(buttonId, 0xFFFFFFFF, false);
        }

        /**
         * Set a custom button texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         *
         * @param buttonId   The ID of the button texture
         * @param textColor  The color of text overlaid on this button
         * @param textShadow If text overlaid on this button should have a text shadow
         */
        public Builder button(String buttonId, int textColor, boolean textShadow) {
            theme.elementBuilder.add(b -> b
                    .add("button", new JsonBuilder()
                            .add("background", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", buttonId))
                            .add("textColor", textColor)
                            .add("textShadow", textShadow)));
            return this;
        }

        /**
         * Set a custom item slot texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         */
        public Builder itemSlot(String itemSlotId) {
            return itemSlot(itemSlotId, 0x60FFFFFF);
        }

        /**
         * Set a custom item slot texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         *
         * @param itemSlotId The ID of the item slot texture
         * @param hoverColor The color of the tooltip hover box for this widget
         */
        public Builder itemSlot(String itemSlotId, int hoverColor) {
            theme.elementBuilder.add(b -> b
                    .add("itemSlot", new JsonBuilder()
                            .add("background", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", itemSlotId))
                            .add("slotHoverColor", hoverColor)));
            return this;
        }

        /**
         * Set a custom fluid slot texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         */
        public Builder fluidSlot(String fluidSlotId) {
            return fluidSlot(fluidSlotId, 0x60FFFFFF);
        }

        /**
         * Set a custom fluid slot texture for UIs with this theme.
         * This ID must correspond with a {@link com.cleanroommc.modularui.drawable.UITexture} that is
         * registered using {@link com.cleanroommc.modularui.drawable.GuiTextures#registerIcon}!
         *
         * @param fluidSlotId The ID of the fluid slot texture
         * @param hoverColor  The color of the tooltip hover box for this widget
         */
        public Builder fluidSlot(String fluidSlotId, int hoverColor) {
            theme.elementBuilder.add(b -> b
                    .add("fluidSlot", new JsonBuilder()
                            .add("background", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", fluidSlotId))
                            .add("slotHoverColor", hoverColor)));
            return this;
        }

        /**
         * Set the text color for text fields in UIs with this theme.
         */
        public Builder textField(int textColor) {
            return textField(textColor, 0xFF2F72A8);
        }

        /**
         * Set the text color for text fields in UIs with this theme.
         *
         * @param textColor   Text color
         * @param markedColor Color of the highlight on selected text
         */
        public Builder textField(int textColor, int markedColor) {
            theme.elementBuilder.add(b -> b
                    .add("textField", new JsonBuilder()
                            .add("textColor", textColor)
                            .add("markedColor", markedColor)));
            return this;
        }

        public Builder toggleButton(String toggleButtonId, String selectedBackgroundId) {
            return toggleButton(toggleButtonId, selectedBackgroundId, 0xFFFFFFFF, true);
        }

        public Builder toggleButton(String toggleButtonId, String selectedBackgroundId, int textColor,
                                    boolean textShadow) {
            return toggleButton(toggleButtonId, selectedBackgroundId, textColor, textShadow, null, 0xFFBBBBBB);
        }

        public Builder toggleButton(String toggleButtonId, String selectedBackgroundId, int textColor,
                                    boolean textShadow, String selectedHoverBackgroundId, int selectedColor) {
            theme.elementBuilder.add(b -> b
                    .add("toggleButton", new JsonBuilder()
                            .add("background", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", toggleButtonId))
                            .add("textColor", textColor)
                            .add("textShadow", textShadow)
                            .add("selectedBackground", new JsonBuilder()
                                    .add("type", "texture")
                                    .add("id", selectedBackgroundId))
                            .add("selectedHoverBackground", selectedHoverBackgroundId)
                            .add("selectedColor", selectedColor)));
            return this;
        }

        public GTGuiTheme build() {
            return theme;
        }
    }
}
