package gregtech.common.covers.filter;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;

import com.cleanroommc.modularui.widget.Widget;

import gregtech.api.util.IDirtyNotifiable;

import org.jetbrains.annotations.NotNull;

public interface Filter<T> {

    /** Uses Cleanroom MUI */
    @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI */
    @NotNull ModularPanel createPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

    @NotNull Widget<?> createWidgets(GuiSyncManager syncManager);

    void match(T toMatch);

    boolean test(T toTest);

    void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable);

    void markDirty();

    void setOnMatched(OnMatch<T> onMatch);

    @FunctionalInterface
    interface OnMatch<T> {
        void onMatch(boolean matched, T match, int matchedSlot);
    }
}
