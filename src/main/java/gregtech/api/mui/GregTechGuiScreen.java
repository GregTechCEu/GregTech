package gregtech.api.mui;

import gregtech.api.GTValues;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

public class GregTechGuiScreen extends ModularScreen {

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(mainPanel, GTGuiTheme.STANDARD);
    }

    public GregTechGuiScreen(ModularPanel mainPanel, GTGuiTheme theme) {
        this(GTValues.MODID, mainPanel, theme);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, GTGuiTheme theme) {
        this(owner, mainPanel, theme.getId());
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, String themeId) {
        super(owner, mainPanel);
        useTheme(themeId);
    }
}
