package gregtech.common.covers.filter;

import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

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
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleItemFilter extends ItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;
    private final SimpleFilterReader filterReader;

    public static final String IGNORE_NBT = "ignore_nbt";
    public static final String IGNORE_DAMAGE = "ignore_damage";

    public SimpleItemFilter(ItemStack stack) {
        this.filterReader = new SimpleFilterReader(stack, MAX_MATCH_SLOTS);
        setFilterReader(this.filterReader);
    }

    @Override
    public MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        int itemFilterMatchIndex = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(), itemStack);
        var result = ItemFilter.createResult(itemFilterMatchIndex == -1 ? Match.FAIL : Match.SUCCEED, itemFilterMatchIndex);
        if (filterReader.isBlacklistFilter()) result.flipMatch();
        return result;
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
        return GTGuis.createPopupPanel("simple_item_filter", 81, 81)
                .child(IKey.str("Settings").asWidget().margin(4).align(Alignment.TopLeft))
                .child(createWidgets(syncManager).bottom(4).left(4));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel("simple_item_filter", 176, 166);
    }

    @Override
    @NotNull
    public ParentWidget<?> createWidgets(GuiSyncManager syncManager) {
        SlotGroup filterInventory = new SlotGroup("filter_inv", 3, 1000, true);
        var blacklist = new BooleanSyncValue(this.filterReader::isBlacklistFilter, this.filterReader::setBlacklistFilter);
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
                                            .getInteger(COUNT);
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
                        .build())
                .child(new Column().width(18).coverChildren()
                        .child(new CycleButtonWidget()
                                .value(blacklist)
                                .textureGetter(state -> GTGuiTextures.BUTTON_BLACKLIST[state])
                                .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                                .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")))
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
    public void writeToNBT(NBTTagCompound tagCompound) {
//        super.writeToNBT(tagCompound);
//        tagCompound.setTag("ItemFilter", itemFilterSlots.serializeNBT());
//        tagCompound.setBoolean("IgnoreDamage", ignoreDamage);
//        tagCompound.setBoolean("IgnoreNBT", ignoreNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
//        super.readFromNBT(tagCompound);
//        this.itemFilterSlots.deserializeNBT(tagCompound.getCompoundTag("ItemFilter"));
//        this.ignoreDamage = tagCompound.getBoolean("IgnoreDamage");
//        this.ignoreNBT = tagCompound.getBoolean("IgnoreNBT");
    }

    public static int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage, boolean ignoreNBTData,
                                      ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData, ItemStack filterStack,
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

    protected class SimpleFilterReader extends BaseFilterReader {
        public SimpleFilterReader(ItemStack container, int slots) {
            super(container, slots);
            setIgnoreDamage(true);
            setIgnoreNBT(true);
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
            return getStackTag().getBoolean(IGNORE_DAMAGE);
        }

        public boolean isIgnoreNBT() {
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
