package gregtech.api.terminal2;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public interface ITerminalApp {

    /**
     * Create the UI for your app.
     */
    IWidget buildWidgets(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings, ModularPanel panel);

    /**
     * @return The drawable that will be used for the icon of your app on the terminal home screen.
     */
    default IDrawable getIcon() {
        return GuiTextures.IMAGE;
    }

    /**
     * Called when the user opens your app from the terminal home screen.
     */
    default void onOpen() {}

    /**
     * Called when the terminal is closed. Free any references to UI elements or sync handlers here.
     */
    default void dispose() {}
}
