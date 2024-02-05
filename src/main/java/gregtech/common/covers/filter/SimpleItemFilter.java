package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
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

public class SimpleItemFilter extends ItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;
    private final SimpleItemFilterReader filterReader;

    public SimpleItemFilter(ItemStack stack) {
        this.filterReader = new SimpleItemFilterReader(stack, MAX_MATCH_SLOTS);
        setFilterReader(this.filterReader);
    }

    @Override
    public MatchResult<ItemStack> match(ItemStack itemStack) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                itemStack);
        return createResult(matchedSlot != -1, filterReader.getStackInSlot(matchedSlot), matchedSlot);
    }

    @Override
    public boolean test(ItemStack toTest) {
        int matchedSlot = itemFilterMatch(filterReader, filterReader.isIgnoreDamage(), filterReader.isIgnoreNBT(),
                toTest);
        return matchedSlot != -1;
    }

    @Override
    public int getTransferLimit(int matchSlot, int globalTransferLimit) {
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
                filterReader::isIgnoreDamage, filterReader::setIgnoreDamage)
                        .setTooltipText("cover.item_filter.ignore_damage"));
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

    @SuppressWarnings("UnstableApiUsage")
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
                                .tooltip(tooltip -> {
                                    tooltip.setAutoUpdate(true);
                                    tooltip.textColor(Color.GREY.main);
                                })
                                .tooltipBuilder(tooltip -> {
                                    tooltip.addLine(IKey.lang("cover.item_filter.config_amount"));
                                    int count = this.filterReader.getItemsNbt()
                                            .getCompoundTagAt(index)
                                            .getInteger(SimpleItemFilterReader.COUNT);
                                    if (count > 64)
                                        tooltip.addLine(
                                                IKey.format("Count: %s", TextFormattingUtil.formatNumbers(count)));
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

    protected class SimpleItemFilterReader extends BaseItemFilterReader {

        public static final String COUNT = "Count";
        public static final String RESPECT_NBT = "IgnoreNBT";
        public static final String RESPECT_DAMAGE = "IgnoreDamage";

        public SimpleItemFilterReader(ItemStack container, int slots) {
            super(container, slots);
        }

        protected void setIgnoreDamage(boolean ignoreDamage) {
            if (!getStackTag().getBoolean(RESPECT_DAMAGE) == ignoreDamage)
                return;

            if (ignoreDamage)
                getStackTag().removeTag(RESPECT_DAMAGE);
            else
                getStackTag().setBoolean(RESPECT_DAMAGE, true);
            markDirty();
        }

        protected void setIgnoreNBT(boolean ignoreNBT) {
            if (!getStackTag().getBoolean(RESPECT_NBT) == ignoreNBT)
                return;

            if (ignoreNBT)
                getStackTag().removeTag(RESPECT_NBT);
            else
                getStackTag().setBoolean(RESPECT_NBT, true);
            markDirty();
        }

        public boolean isIgnoreDamage() {
            return !getStackTag().getBoolean(RESPECT_DAMAGE);
        }

        public boolean isIgnoreNBT() {
            return !getStackTag().getBoolean(RESPECT_NBT);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getMaxTransferRate();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!stack.isEmpty()) {
                stack.setCount(Math.min(stack.getCount(), isBlacklistFilter() ? 1 : getMaxTransferRate()));
            }
            super.setStackInSlot(slot, stack);
        }

        @Override
        public void onTransferRateChange() {
            for (int i = 0; i < getSlots(); i++) {
                ItemStack itemStack = getStackInSlot(i);
                if (!itemStack.isEmpty()) {
                    itemStack.setCount(Math.min(itemStack.getCount(), isBlacklistFilter() ? 1 : getMaxTransferRate()));
                    setStackInSlot(i, itemStack);
                }
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound tagCompound) {
            super.readFromNBT(tagCompound);
            this.setIgnoreDamage(tagCompound.getBoolean(RESPECT_DAMAGE));
            this.setIgnoreNBT(tagCompound.getBoolean(RESPECT_NBT));

            if (tagCompound.hasKey(KEY_ITEMS)) {
                var temp = new ItemStackHandler();
                temp.deserializeNBT(tagCompound.getCompoundTag(KEY_ITEMS));
                for (int i = 0; i < temp.getSlots(); i++) {
                    var stack = temp.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    this.setStackInSlot(i, stack);
                }
            }
        }
    }
}
