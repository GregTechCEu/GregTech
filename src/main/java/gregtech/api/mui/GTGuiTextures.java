package gregtech.api.mui;

import com.cleanroommc.modularui.drawable.UITexture;

import gregtech.api.GTValues;

public class GTGuiTextures {

    // Keys used for GT assets registered for use in Themes
    public static class IDs {
        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
        public static final String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        public static final String STEEL_BACKGROUND = "gregtech_steel_bg";
        public static final String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";
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

    public static void init() {/**/}
}
