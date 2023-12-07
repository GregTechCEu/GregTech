package gregtech.api.mui;

import gregtech.api.GTValues;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

// todo have GT API classes provide a getTheme() method so that
// todo they don't have to override createScreen() themselves.
public class GregTechGuiScreen extends ModularScreen {

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(mainPanel, GTThemes.STANDARD);
    }

    public GregTechGuiScreen(ModularPanel mainPanel, GTThemes theme) {
        this(GTValues.MODID, mainPanel, theme);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, GTThemes theme) {
        this(owner, mainPanel, theme.getId());
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, String themeId) {
        super(owner, mainPanel);
        useTheme(themeId);
    }
}
