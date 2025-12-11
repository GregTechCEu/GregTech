package gregtech.common.items.behaviors.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.covers.filter.BaseFilter;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

import gregtech.common.covers.filter.readers.SimpleItemFilterReader;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class SimpleItemFilterUIManager extends BaseFilterUIManager {

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        return createPanel(guiData.getUsedItemStack(), guiSyncManager);
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return GTGuis.createPopupPanel(panelName, 98, 81, false)
                .child(CoverWithUI.createTitleRow(stack))
                .child(createWidgets(stack, syncManager).top(22).left(4));
    }

    @Override
    public @NotNull ModularPanel createPanel(ItemStack stack, PanelSyncManager syncManager) {
        return GTGuis.createPanel("simple_item_filter", 176, 166);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Widget<?> createWidgets(ItemStack stack, PanelSyncManager syncManager) {
        SimpleItemFilterReader filterReader = (SimpleItemFilterReader) BaseFilter.getFilterFromStack(stack).getFilterReader();
        SlotGroup filterInventory = new SlotGroup("filter_inv", 3, 1000, true);
        var ignoreDamage = new BooleanSyncValue(filterReader::isIgnoreDamage, filterReader::setIgnoreDamage);
        var ignoreNBT = new BooleanSyncValue(filterReader::isIgnoreNBT, filterReader::setIgnoreNBT);

        syncManager.registerSlotGroup(filterInventory);

        return Flow.row().coverChildren()
                .child(SlotGroupWidget.builder()
                        .matrix("XXX",
                                "XXX",
                                "XXX")
                        .key('X', index -> new PhantomItemSlot()
                                .slot(SyncHandlers.itemSlot(filterReader, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup(filterInventory)
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                            if (onlyAmountChanged && !init) {
                                                filterReader.markDirty();
                                            }
                                        }))
                                .tooltipAutoUpdate(true)
                                .tooltipTextColor(Color.GREY.main)
                                .tooltipBuilder(tooltip -> {
                                    if (filterReader.shouldShowCount()) {
                                        int count = filterReader.getStackCountInSlot(index);
                                        if (count > 0) {
                                            tooltip.addLine(IKey.lang("cover.item_filter.config_amount"));
                                            tooltip.addLine(
                                                    IKey.str("Count: %s", TextFormattingUtil.formatNumbers(count)));
                                        }
                                    }
                                }))
                        .build().marginRight(4))
                .child(Flow.column().width(18).coverChildren()
                        .child(createBlacklistUI(stack))
                        .child(new CycleButtonWidget()
                                .value(ignoreDamage)
                                .stateBackground(0, GTGuiTextures.BUTTON_IGNORE_DAMAGE[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_IGNORE_DAMAGE[1])
                                .addTooltip(0, IKey.lang("cover.item_filter.ignore_damage.disabled"))
                                .addTooltip(1, IKey.lang("cover.item_filter.ignore_damage.enabled")))
                        .child(new CycleButtonWidget()
                                .value(ignoreNBT)
                                .stateBackground(0, GTGuiTextures.BUTTON_IGNORE_NBT[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_IGNORE_NBT[1])
                                .addTooltip(0, IKey.lang("cover.item_filter.ignore_nbt.disabled"))
                                .addTooltip(1, IKey.lang("cover.item_filter.ignore_nbt.enabled"))));
    }
}
