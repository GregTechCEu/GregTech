package gregtech.api.util;

import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import gregtech.mixins.mui2.PanelSyncHandlerAccessor;
import gregtech.mixins.mui2.PanelSyncManagerAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MUIUtil {

    /**
     * Checks if the {@link PanelSyncManager} has any sub panels.
     * @param panelSyncManager the panel sync manager to check
     * @return if it has sub panels
     */
    public static boolean hasSubPanels(PanelSyncManager panelSyncManager) {
        return !((PanelSyncManagerAccessor) panelSyncManager).getSubPanels().isEmpty();
    }

    /**
     * Retrieves all sub panel {@link PanelSyncHandler}s from a {@link PanelSyncManager}.
     * @param panelSyncManager the panel sync manager to gather the sub panel handlers from
     * @return a list of all sub panel handlers
     */
    public static List<PanelSyncHandler> getSubPanels(PanelSyncManager panelSyncManager) {
        if (!hasSubPanels(panelSyncManager)) {
            return Collections.emptyList();
        }

        List<PanelSyncHandler> subPanels = new ArrayList<>();
        Collection<SyncHandler> syncHandlers = ((PanelSyncManagerAccessor) panelSyncManager).getSubPanels().values();

        for (SyncHandler syncHandler : syncHandlers) {
            if (syncHandler instanceof PanelSyncHandler panelSyncHandler) {
                subPanels.add(panelSyncHandler);
            }
        }

        return subPanels;
    }

    /**
     * Gets all of the {@link SyncHandler}s from a {@link PanelSyncManager}.
     * @param panelSyncManager the panel sync manager to gather the sync handlers from
     * @return a list of all the sync handlers
     */
    public static Collection<SyncHandler> getSyncHandlers(PanelSyncManager panelSyncManager) {
        return ((PanelSyncManagerAccessor) panelSyncManager).getSyncHandlers().values();
    }

    /**
     * Checks if a {@link PanelSyncHandler} has a {@link PanelSyncManager}.
     * @param panelSyncHandler the panel sync handler to check
     * @return if the panel sync handler has a panel sync manager
     */
    public static boolean hasSyncManager(PanelSyncHandler panelSyncHandler) {
        return ((PanelSyncHandlerAccessor) (Object) panelSyncHandler).getPanelSyncManager() != null;
    }

    /**
     * Gets the {@link PanelSyncManager} from a {@link PanelSyncHandler}.
     * @param panelSyncHandler the panel sync handler to get the panel sync manager from
     * @return the panel sync manager of the panel sync handler
     */
    public static PanelSyncManager getPanelSyncManager(PanelSyncHandler panelSyncHandler) {
        return ((PanelSyncHandlerAccessor) (Object) panelSyncHandler).getPanelSyncManager();
    }
}
