package gregtech.api.capability;

import gregtech.common.mui.widget.ScrollableTextWidget;

import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.vec.Cuboid6;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IMiner {

    Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    boolean drainEnergy(boolean simulate);

    default boolean drainFluid(boolean simulate) {
        return true;
    }

    boolean isInventoryFull();

    void setInventoryFull(boolean isFull);

    default int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }

    default Widget<?> createMinerWidgets(@NotNull PanelSyncManager panelSyncManager,
                                         @NotNull IItemHandlerModifiable inventory, int inventorySize,
                                         @NotNull UITexture textDisplayBackground,
                                         @NotNull Consumer<IRichTextBuilder<?>> textBuilder) {
        int rowSize = (int) Math.sqrt(inventorySize);
        panelSyncManager.registerSlotGroup("export_items", rowSize);

        return Flow.row()
                .coverChildren()
                .child(new ScrollableTextWidget()
                        .size(105 - 3 * 2, 75 - 3 * 2)
                        .autoUpdate(true)
                        .alignment(Alignment.TopLeft)
                        .textBuilder(textBuilder)
                        .background(textDisplayBackground.asIcon()
                                .margin(-3)))
                .child(new Grid()
                        .marginLeft(6)
                        .minElementMargin(0)
                        .minColWidth(18)
                        .minRowHeight(18)
                        .mapTo(rowSize, inventorySize, index -> new ItemSlot()
                                .slot(SyncHandlers.itemSlot(inventory, index)
                                        .slotGroup("export_items")
                                        .accessibility(false, true))));
    }
}
