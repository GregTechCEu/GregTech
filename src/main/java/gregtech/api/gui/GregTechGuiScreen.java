package gregtech.api.gui;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import gregtech.api.GTValues;

public class GregTechGuiScreen extends ModularScreen {

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(GTValues.MODID, mainPanel);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel) {
        super(owner, mainPanel);
        this.context.useTheme("gregtech");
    }

    public GregTechGuiScreen withTheme(String theme) {
        this.context.useTheme(theme);
        return this;
    }
}
