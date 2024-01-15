package gregtech.common.covers.filter;

import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Column;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.slot.PhantomItemSlot;

import gregtech.api.util.TextFormattingUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleItemFilter extends ItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;
    private final SimpleItemFilterReader filterReader;

    public SimpleItemFilter(ItemStack stack) {
        this.filterReader = new SimpleItemFilterReader(stack, MAX_MATCH_SLOTS);
        setFilterReader(this.filterReader);
    }

    @Override
    public void match(ItemStack itemStack) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(), itemStack);
        this.onMatch(matchedSlot != -1, itemStack.copy(), matchedSlot);
    }

    @Override
    public boolean test(ItemStack toTest) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(), toTest);
        return matchedSlot != -1;
    }

    @Override
    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        ItemStack stackInFilterSlot = filterReader.getStackInSlot(matchSlot);
        return Math.min(stackInFilterSlot.getCount(), globalTransferLimit);
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; i++) {
            widgetGroup.accept(new PhantomSlotWidget(filterReader, i, 10 + 18 * (i % 3), 18 * (i / 3))
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        widgetGroup.accept(new ToggleButtonWidget(74, 0, 20, 20, GuiTextures.BUTTON_FILTER_DAMAGE,
                filterReader::isIgnoreDamage, filterReader::setIgnoreDamage).setTooltipText("cover.item_filter.ignore_damage"));
        widgetGroup.accept(new ToggleButtonWidget(99, 0, 20, 20, GuiTextures.BUTTON_FILTER_NBT,
                filterReader::isIgnoreNBT, filterReader::setIgnoreNBT).setTooltipText("cover.item_filter.ignore_nbt"));
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_item_filter", 98, 81)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22).left(4));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel("simple_item_filter", 176, 166);
    }

    @Override
    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
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
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                                .tooltipBuilder(tooltip -> {
                                    int count = this.filterReader.getItemsNbt()
                                            .getCompoundTagAt(index)
                                            .getInteger(SimpleItemFilterReader.COUNT);
                                    if (count > 64)
                                        tooltip.addLine(IKey.format("Count: %s", TextFormattingUtil.formatNumbers(count)));
                                })
                                .slot(new PhantomItemSlot(this.filterReader, index, getMaxStackSizer())
                                        .slotGroup(filterInventory)
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                            if (onlyAmountChanged && !init) {
                                                markDirty();
                                            }
                                        })))
                        .build().marginRight(4))
                .child(new Column().width(18).coverChildren()
                        .child(super.createWidgets(syncManager))
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

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        if (tagCompound.hasKey("ItemFilter")) {
            var temp = new ItemStackHandler();
            temp.deserializeNBT(tagCompound.getCompoundTag("ItemFilter"));
            for (int i = 0; i < temp.getSlots(); i++) {
                var stack = temp.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                this.filterReader.setStackInSlot(i, stack);
            }
        }

        this.filterReader.setIgnoreDamage(tagCompound.getBoolean("IgnoreDamage"));
        this.filterReader.setIgnoreNBT(tagCompound.getBoolean("IgnoreNBT"));
    }

    public int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage, boolean ignoreNBTData,
                                      ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData, ItemStack filterStack,
                                         ItemStack itemStack) {
        if (ignoreDamage) {
            if (!filterStack.isItemEqualIgnoreDurability(itemStack)) {
                return false;
            }
        } else if (!filterStack.isItemEqual(itemStack)) {
            return false;
        }
        return ignoreNBTData || ItemStack.areItemStackTagsEqual(filterStack, itemStack);
    }

    protected class SimpleItemFilterReader extends BaseItemFilterReader {

        public static final String IGNORE_NBT = "ignore_nbt";
        public static final String IGNORE_DAMAGE = "ignore_damage";
        public SimpleItemFilterReader(ItemStack container, int slots) {
            super(container, slots);
        }

        protected void setIgnoreDamage(boolean ignoreDamage) {
            getStackTag().setBoolean(IGNORE_DAMAGE, ignoreDamage);
            markDirty();
        }

        protected void setIgnoreNBT(boolean ignoreNBT) {
            getStackTag().setBoolean(IGNORE_NBT, ignoreNBT);
            markDirty();
        }

        public boolean isIgnoreDamage() {
            if (!getStackTag().hasKey(IGNORE_DAMAGE))
                setIgnoreDamage(true);

            return getStackTag().getBoolean(IGNORE_DAMAGE);
        }

        public boolean isIgnoreNBT() {
            if (!getStackTag().hasKey(IGNORE_NBT))
                setIgnoreNBT(true);

            return getStackTag().getBoolean(IGNORE_NBT);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getMaxStackSize();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!stack.isEmpty()) {
                stack.setCount(Math.min(stack.getCount(), getMaxStackSize()));
            }
            super.setStackInSlot(slot, stack);
        }

        @Override
        public void onMaxStackSizeChange() {
            super.onMaxStackSizeChange();
            for (int i = 0; i < getSlots(); i++) {
                ItemStack itemStack = getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    itemStack.setCount(Math.min(itemStack.getCount(), getMaxStackSize()));
                    setStackInSlot(i, itemStack);
                }
            }
        }
    }
}
