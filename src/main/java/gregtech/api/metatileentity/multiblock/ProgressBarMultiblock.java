package gregtech.api.metatileentity.multiblock;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import java.util.List;
import java.util.function.UnaryOperator;

public interface ProgressBarMultiblock {

    int getProgressBarCount();

    // the bar only needs three things
    // progress, texture, and tooltip
    void registerBars(List<UnaryOperator<ProgressWidget>> bars, PanelSyncManager syncManager);

    default boolean hasBars() {
        return getProgressBarCount() > 0;
    }
}
