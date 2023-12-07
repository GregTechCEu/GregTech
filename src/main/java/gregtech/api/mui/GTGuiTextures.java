package gregtech.api.mui;

import com.cleanroommc.modularui.drawable.UITexture;

import gregtech.api.GTValues;

// TODO: Move primitive slot and background to "textures/gui/base"
public class GTGuiTextures {

    // Keys used for GT assets registered for use in Themes
    public static class IDs {
        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
        public static final String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        public static final String STEEL_BACKGROUND = "gregtech_steel_bg";
        public static final String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";

        public static final String BRONZE_SLOT = "gregtech_bronze_slot";
        public static final String STEEL_SLOT = "gregtech_steel_slot";
        public static final String PRIMITIVE_SLOT = "gregtech_primitive_slot";
    }

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

    public static void init() {/**/}
}
