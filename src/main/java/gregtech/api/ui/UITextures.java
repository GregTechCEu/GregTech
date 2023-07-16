package gregtech.api.ui;

import com.cleanroommc.modularui.drawable.UITexture;
import gregtech.api.GTValues;

public final class UITextures {

    public static final String GT_BACKGROUND = "gt_background";
    public static final String GT_DISPLAY = "gt_display";

    public static UITexture PROGRESS_BAR_ARROW;
    public static final String PROGRESS_BAR_ARROW_KEY = "progress_bar_arrow";

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
        PROGRESS_BAR_ARROW = UITexture.builder()
                .location(GTValues.MODID, "gui/progress_bar/progress_bar_arrow")
                .imageSize(20, 40)
                .registerAsIcon(PROGRESS_BAR_ARROW_KEY)
                .build();
    }
}
