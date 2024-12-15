package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.covers.CoverItemVoidingAdvanced;
import gregtech.common.covers.CoverRoboticArm;
import gregtech.common.covers.TransferMode;
import gregtech.common.covers.VoidingMode;
import gregtech.common.covers.filter.readers.SimpleItemFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleItemFilter extends BaseFilter {

    private static final int MAX_MATCH_SLOTS = 9;
    private final SimpleItemFilterReader filterReader;

    public SimpleItemFilter(ItemStack stack) {
        filterReader = new SimpleItemFilterReader(stack, MAX_MATCH_SLOTS);
    }

    @Override
    public SimpleItemFilterReader getFilterReader() {
        return filterReader;
    }

    @Override
    public MatchResult matchItem(ItemStack itemStack) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                itemStack);
        return MatchResult.create(matchedSlot != -1 == !isBlacklistFilter(),
                filterReader.getStackInSlot(matchedSlot), matchedSlot);
    }

    @Override
    public boolean testItem(ItemStack toTest) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                toTest);
        return matchedSlot != -1;
    }

    @Override
    public int getTransferLimit(int matchSlot, int transferSize) {
        ItemStack stackInFilterSlot = filterReader.getStackInSlot(matchSlot);
        return Math.min(stackInFilterSlot.getCount(), transferSize);
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM;
    }

    @Override
    public int getTransferLimit(ItemStack stack, int transferSize) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                stack);
        return getTransferLimit(matchedSlot, transferSize);
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; i++) {
            widgetGroup.accept(new PhantomSlotWidget(filterReader, i, 10 + 18 * (i % 3), 18 * (i / 3))
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        widgetGroup.accept(new ToggleButtonWidget(74, 0, 20, 20, GuiTextures.BUTTON_FILTER_DAMAGE,
                filterReader::isIgnoreDamage, filterReader::setIgnoreDamage)
                        .setTooltipText("cover.item_filter.ignore_damage"));
        widgetGroup.accept(new ToggleButtonWidget(99, 0, 20, 20, GuiTextures.BUTTON_FILTER_NBT,
                filterReader::isIgnoreNBT, filterReader::setIgnoreNBT).setTooltipText("cover.item_filter.ignore_nbt"));
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(PanelSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_item_filter", 98, 81)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22).left(4));
    }

    @Override
    public @NotNull ModularPanel createPanel(PanelSyncManager syncManager) {
        return GTGuis.createPanel("simple_item_filter", 176, 166);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Widget<?> createWidgets(PanelSyncManager syncManager) {
        SlotGroup filterInventory = new SlotGroup("filter_inv", 3, 1000, true);
        var ignoreDamage = new BooleanSyncValue(this.filterReader::isIgnoreDamage, this.filterReader::setIgnoreDamage);
        var ignoreNBT = new BooleanSyncValue(this.filterReader::isIgnoreNBT, this.filterReader::setIgnoreNBT);

        syncManager.registerSlotGroup(filterInventory);

        return new Row().coverChildren()
                .child(SlotGroupWidget.builder()
                        .matrix("XXX",
                                "XXX",
                                "XXX")
                        .key('X', index -> new ItemSlot()
                                .tooltip(tooltip -> {
                                    tooltip.setAutoUpdate(true);
                                    tooltip.textColor(Color.GREY.main);
                                })
                                .tooltipBuilder(tooltip -> {
                                    if (dirtyNotifiable instanceof CoverRoboticArm coverArm &&
                                            coverArm.getTransferMode() != TransferMode.TRANSFER_ANY ||
                                            dirtyNotifiable instanceof CoverItemVoidingAdvanced coverItem &&
                                                    coverItem.getVoidingMode() != VoidingMode.VOID_ANY) {
                                        tooltip.addLine(IKey.lang("cover.item_filter.config_amount"));
                                        int count = this.filterReader.getTagAt(index)
                                                .getInteger(SimpleItemFilterReader.COUNT);
                                        if (count > 0)
                                            tooltip.addLine(
                                                    IKey.format("Count: %s", TextFormattingUtil.formatNumbers(count)));
                                    }
                                })
                                .slot(SyncHandlers.phantomItemSlot(this.filterReader, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup(filterInventory)
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                            if (onlyAmountChanged && !init) {
                                                markDirty();
                                            }
                                        })))
                        .build().marginRight(4))
                .child(new Column().width(18).coverChildren()
                        .child(createBlacklistUI())
                        .child(new CycleButtonWidget()
                                .value(ignoreDamage)
                                .textureGetter(state -> GTGuiTextures.BUTTON_IGNORE_DAMAGE[state])
                                .addTooltip(0, IKey.lang("cover.item_filter.ignore_damage.disabled"))
                                .addTooltip(1, IKey.lang("cover.item_filter.ignore_damage.enabled")))
                        .child(new CycleButtonWidget()
                                .value(ignoreNBT)
                                .textureGetter(state -> GTGuiTextures.BUTTON_IGNORE_NBT[state])
                                .addTooltip(0, IKey.lang("cover.item_filter.ignore_nbt.disabled"))
                                .addTooltip(1, IKey.lang("cover.item_filter.ignore_nbt.enabled"))));
    }

    public static int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage,
                                      boolean ignoreNBTData, ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData,
                                         ItemStack filterStack, ItemStack itemStack) {
        if (ignoreDamage) {
            if (!filterStack.isItemEqualIgnoreDurability(itemStack)) {
                return false;
            }
        } else if (!filterStack.isItemEqual(itemStack)) {
            return false;
        }
        return ignoreNBTData || ItemStack.areItemStackTagsEqual(filterStack, itemStack);
    }
}
