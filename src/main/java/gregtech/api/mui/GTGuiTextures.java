package gregtech.api.mui;

import com.cleanroommc.modularui.drawable.UITexture;

import gregtech.api.GTValues;

public class GTGuiTextures {

    // Keys used for GT assets registered for use in Themes
    public static class IDs {
        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
    }

    // BASE TEXTURES
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .registerAsBackground(IDs.STANDARD_BACKGROUND, true)
            .build();

    public static void init() {/**/}
}
