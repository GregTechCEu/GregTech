package gregtech.api.mui;

import com.cleanroommc.modularui.drawable.UITexture;

import gregtech.api.GTValues;

public class GTGuiTextures {

    //BASE TEXTURES
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTValues.MODID, "textures/gui/base/background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .canApplyTheme()
            .build();
}
