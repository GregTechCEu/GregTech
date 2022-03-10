package gregtech.api.gui;

import com.cleanroommc.modularui.api.drawable.AdaptableUITexture;
import com.cleanroommc.modularui.api.drawable.UITexture;
import gregtech.api.GTValues;

public class GuiTextures {

    public static final AdaptableUITexture BACKGROUND = AdaptableUITexture.of(GTValues.MODID, "gui/base/background", 176, 166, 3);
    public static final AdaptableUITexture DISPLAY = AdaptableUITexture.of(GTValues.MODID, "gui/base/display", 143, 75, 2);

    // Base
    public static final UITexture BASE_BUTTON = UITexture.fullImage(GTValues.MODID, "gui/widget/button_overclock");
    public static final UITexture VANILLA_BUTTON = UITexture.fullImage(GTValues.MODID, "gui/widget/vanilla_button");
    public static final UITexture VANILLA_BUTTON_NORMAL = VANILLA_BUTTON.getSubArea(0, 0, 1, 0.5f);
    public static final UITexture VANILLA_BUTTON_ACTIVE = VANILLA_BUTTON.getSubArea(0, 0.5f,1, 1);

    public static final UITexture DISTRIBUTION_MODE = UITexture.fullImage("gui/widget/button_distribution_mode");
}
