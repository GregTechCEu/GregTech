package gregtech.api.gui;

import com.cleanroommc.modularui.api.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.api.drawable.UITexture;
import gregtech.api.GTValues;

public class GuiTextures {

    public static final AdaptableUITexture BACKGROUND = AdaptableUITexture.of(GTValues.MODID, "gui/base/background", 176, 166, 3);
    public static final AdaptableUITexture DISPLAY = AdaptableUITexture.of(GTValues.MODID, "gui/base/display", 143, 75, 2);
    public static final AdaptableUITexture BASE_BUTTON = AdaptableUITexture.of(GTValues.MODID, "gui/widget/button_overclock", 18, 18, 1);

    public static final UITexture DISTRIBUTION_MODE = UITexture.fullImage(GTValues.MODID, "gui/widget/button_distribution_mode");
}
