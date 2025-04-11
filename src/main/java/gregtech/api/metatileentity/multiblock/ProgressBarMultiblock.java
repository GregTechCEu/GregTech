package gregtech.api.metatileentity.multiblock;

import gregtech.api.metatileentity.multiblock.ui.TemplateBarBuilder;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import java.util.List;
import java.util.function.UnaryOperator;

public interface ProgressBarMultiblock {

    int getProgressBarCount();

    // the bar only needs three things
    // progress, texture, and tooltip
    void registerBars(List<UnaryOperator<TemplateBarBuilder>> bars, PanelSyncManager syncManager);
}
