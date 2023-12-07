package gregtech.api.mui;

import gregtech.api.GTValues;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

public class GregTechGuiScreen extends ModularScreen {

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(GTValues.MODID, mainPanel);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel) {
        super(owner, mainPanel);
        useTheme("gregtech");
    }
}
