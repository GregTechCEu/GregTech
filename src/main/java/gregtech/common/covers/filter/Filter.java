package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public interface Filter<T> {

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPopupPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

    @NotNull
    Widget<?> createWidgets(GuiSyncManager syncManager);

    MatchResult<T> match(T toMatch);

    boolean test(T toTest);

    void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable);

    void markDirty();

    int getMaxTransferSize();

    void setMaxTransferSize(int maxTransferSize);

    boolean showGlobalTransferLimitSlider();

    int getTransferLimit(int slot, int transferSize);

    int getTransferLimit(T stack, int transferSize);

    boolean isBlacklistFilter();

    void setBlacklistFilter(boolean blacklistFilter);
}
