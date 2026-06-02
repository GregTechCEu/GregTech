package gregtech.api.mui.sync;

import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.IJEIRecipeReceiver;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

public abstract class RecipeSyncHandler extends SyncHandler implements IJEIRecipeReceiver {

    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        GregTechGuiScreen.registerRecipeReceiver(getKey(), this);
    }

    @Override
    public void dispose() {
        GregTechGuiScreen.removeRecipeReceiver(getKey());
        super.dispose();
    }
}
