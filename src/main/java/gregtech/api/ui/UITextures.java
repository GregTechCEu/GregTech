package gregtech.api.ui;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.api.GTValues;

public final class UITextures {

    public static final String GT_BACKGROUND = "gt_background";
    public static final String GT_DISPLAY = "gt_display";

    public static void init() {
        UITexture.builder()
                .location(GTValues.MODID, "gui/base/background")
                .imageSize(176, 166)
                .adaptable(3)
                .registerAsBackground(GT_BACKGROUND)
                .build();
        UITexture.builder()
                .location(GTValues.MODID, "gui/base/display")
                .imageSize(143, 75)
                .adaptable(2)
                .registerAsIcon(GT_DISPLAY)
                .build();
    }
}
