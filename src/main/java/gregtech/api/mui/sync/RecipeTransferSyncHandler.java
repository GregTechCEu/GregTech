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
        GregTechGuiScreen.registerRecipeTransferHandler(getKey(), this, getTransferHandlerPriority());
    }

    protected int getTransferHandlerPriority() {
        return 0;
    }

    @ApiStatus.OverrideOnly
    @MustBeInvokedByOverriders
    @Override
    public void dispose() {
        GregTechGuiScreen.removeRecipeTransferHandler(getKey());
        super.dispose();
    }
}
