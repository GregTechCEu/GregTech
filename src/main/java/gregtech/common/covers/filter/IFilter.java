package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFilter {

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPopupPanel(PanelSyncManager syncManager, String panelName);

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPanel(PanelSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

    @NotNull
    Widget<?> createWidgets(PanelSyncManager syncManager);

    default IPanelHandler createPanelHandler(PanelSyncManager syncManager, int id) {
        String translationKey = getContainerStack().getTranslationKey();
        return syncManager.getOrCreateSyncHandler(translationKey, id, PanelSyncHandler.class, () -> {
            String key = PanelSyncManager.makeSyncKey(translationKey, id);
            return (PanelSyncHandler) syncManager.panel(key, (psm, $) -> createPopupPanel(psm, key), true);
        });
    }

    ItemStack getContainerStack();

    void setDirtyNotifiable(@Nullable IDirtyNotifiable dirtyNotifiable);

    void markDirty();

    int getMaxTransferSize();

    void setMaxTransferSize(int maxTransferSize);

    boolean showGlobalTransferLimitSlider();

    MatchResult match(Object toMatch);

    boolean test(Object toTest);

    int getTransferLimit(Object stack, int transferSize);

    default int getTransferLimit(int slot, int transferSize) {
        return transferSize;
    }

    void readFromNBT(NBTTagCompound tagCompound);

    FilterType getType();

    enum FilterType {
        ITEM,
        FLUID
    }

    // this only exists so i can pass in the constructor reference as a metaitem componant
    static Factory factory(Factory factory) {
        return factory;
    }

    @FunctionalInterface
    interface Factory extends IItemComponent {

        @NotNull
        BaseFilter create(@NotNull ItemStack stack);
    }
}
