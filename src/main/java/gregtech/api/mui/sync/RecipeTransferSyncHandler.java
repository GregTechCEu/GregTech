package gregtech.api.mui.sync;

import gregtech.api.mui.GregTechGuiScreen;
import gregtech.api.mui.IRecipeTransferReceiver;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * A base class for to handle implementing {@link IRecipeTransferReceiver} on a {@link SyncHandler}s to automatically
 * register and unregister it from the map of valid handlers in {@link GregTechGuiScreen}.
 */
public abstract class RecipeTransferSyncHandler extends SyncHandler implements IRecipeTransferReceiver {

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    @Override
    public void init(String key, PanelSyncManager syncManager) {
        super.init(key, syncManager);
        if (syncManager.isClient()) {
            GregTechGuiScreen.registerRecipeTransferHandler(getKey(), this, getTransferHandlerPriority());
        }
    }

    protected int getTransferHandlerPriority() {
        return 0;
    }

    // TODO: this method is never actually called. Either fix on our side or wait for it to get fixed in MUI.
    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        if (getSyncManager().isClient()) {
            GregTechGuiScreen.removeRecipeTransferHandler(getKey());
        }
        super.dispose();
    }
}
