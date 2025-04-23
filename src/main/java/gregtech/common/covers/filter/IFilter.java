package gregtech.common.covers.filter;

import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IFilter {

    // this only exists so i can pass in the constructor reference as a metaitem componant
    static Factory factory(Factory factory) {
        return factory;
    }

    @Deprecated
    default void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {}

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPopupPanel(PanelSyncManager syncManager);

    /** Uses Cleanroom MUI */
    @NotNull
    ModularPanel createPanel(PanelSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

    @NotNull
    Widget<?> createWidgets(PanelSyncManager syncManager);

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

    @FunctionalInterface
    interface Factory extends IItemComponent {

        @NotNull
        BaseFilter create(@NotNull ItemStack stack);
    }
}
