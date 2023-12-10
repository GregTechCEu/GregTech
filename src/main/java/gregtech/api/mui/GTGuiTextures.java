package gregtech.api.mui;

import gregtech.api.GTValues;

import com.cleanroommc.modularui.drawable.UITexture;

// TODO: Move primitive slot and background to "textures/gui/base"
public class GTGuiTextures {

    // Keys used for GT assets registered for use in Themes
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
        public static final String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        public static final String STEEL_BACKGROUND = "gregtech_steel_bg";
        public static final String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";

        public static final String STANDARD_SLOT = "gregtech_standard_slot";
        public static final String BRONZE_SLOT = "gregtech_bronze_slot";
        public static final String STEEL_SLOT = "gregtech_steel_slot";
        public static final String PRIMITIVE_SLOT = "gregtech_primitive_slot";

        public static final String STANDARD_BUTTON = "gregtech_standard_button";
    }

    // GT LOGOS
    public static final UITexture GREGTECH_LOGO = UITexture.fullImage(GTValues.MODID,
            "textures/gui/icon/gregtech_logo.png");

    // BACKGROUNDS
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.STANDARD_BACKGROUND, true)
            .build();

    public static final UITexture BACKGROUND_BRONZE = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_bronze.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.BRONZE_BACKGROUND)
            .build();

    public static final UITexture BACKGROUND_STEEL = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background_steel.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.STEEL_BACKGROUND)
            .build();

    public static final UITexture BACKGROUND_PRIMITIVE = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/primitive/primitive_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.PRIMITIVE_BACKGROUND)
            .build();

    // SLOTS
    public static final UITexture SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.STANDARD_SLOT, true)
            .build();

    public static final UITexture SLOT_BRONZE = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot_bronze.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.BRONZE_SLOT)
            .build();

    public static final UITexture SLOT_STEEL = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot_steel.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.STEEL_SLOT)
            .build();

    public static final UITexture SLOT_PRIMITIVE = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/primitive/primitive_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsBackground(IDs.PRIMITIVE_SLOT)
            .build();

    // SLOT OVERLAYS

    public static final UITexture INT_CIRCUIT_OVERLAY = UITexture.fullImage(GTValues.MODID,
            "textures/gui/overlay/int_circuit_overlay.png", true);

    // BUTTONS

    public static final UITexture BUTTON = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/widget/button.png")
            .imageSize(18, 18)
            .adaptable(1)
            .registerAsIcon(IDs.STANDARD_BUTTON)
            .canApplyTheme()
            .build();

    public static final UITexture BUTTON_ITEM_OUTPUT = UITexture.fullImage(GTValues.MODID,
            "textures/gui/widget/button_item_output_overlay.png");

    public static final UITexture BUTTON_AUTO_COLLAPSE = UITexture.fullImage(GTValues.MODID,
            "textures/gui/widget/button_auto_collapse_overlay.png");

    public static final UITexture BUTTON_X = UITexture.fullImage(GTValues.MODID,
            "textures/gui/widget/button_x_overlay.png", true);

    public static void init() {/**/}
}
