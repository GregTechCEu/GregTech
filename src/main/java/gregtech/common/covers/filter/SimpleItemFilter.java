package gregtech.common.covers.filter;

import com.cleanroommc.modularui.screen.ModularPanel;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleItemFilter extends ItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;

    protected final ItemStackHandler itemFilterSlots;
    protected boolean ignoreDamage = true;
    protected boolean ignoreNBT = true;

    public SimpleItemFilter() {
        this.itemFilterSlots = new ItemStackHandler(MAX_MATCH_SLOTS) {

            @Override
            public int getSlotLimit(int slot) {
                return getMaxStackSize();
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
            }
        };
    }

    @Override
    protected void onMaxStackSizeChange() {
        for (int i = 0; i < itemFilterSlots.getSlots(); i++) {
            ItemStack itemStack = itemFilterSlots.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                itemStack.setCount(Math.min(itemStack.getCount(), getMaxStackSize()));
            }
        }
    }

    public ItemStackHandler getItemFilterSlots() {
        return itemFilterSlots;
    }

    public boolean isIgnoreDamage() {
        return ignoreDamage;
    }

    public boolean isIgnoreNBT() {
        return ignoreNBT;
    }

    protected void setIgnoreDamage(boolean ignoreDamage) {
        this.ignoreDamage = ignoreDamage;
        markDirty();
    }

    protected void setIgnoreNBT(boolean ignoreNBT) {
        this.ignoreNBT = ignoreNBT;
        markDirty();
    }

    @Override
    public MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        int itemFilterMatchIndex = itemFilterMatch(getItemFilterSlots(), isIgnoreDamage(), isIgnoreNBT(), itemStack);
        return ItemFilter.createResult(itemFilterMatchIndex == -1 ? Match.FAIL : Match.SUCCEED, itemFilterMatchIndex);
    }

    @Override
    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        ItemStack stackInFilterSlot = itemFilterSlots.getStackInSlot(matchSlot);
        return Math.min(stackInFilterSlot.getCount(), globalTransferLimit);
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public int getTotalOccupiedHeight() {
        return 36;
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; i++) {
            widgetGroup.accept(new PhantomSlotWidget(itemFilterSlots, i, 10 + 18 * (i % 3), 18 * (i / 3))
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        widgetGroup.accept(new ToggleButtonWidget(74, 0, 20, 20, GuiTextures.BUTTON_FILTER_DAMAGE,
                () -> ignoreDamage, this::setIgnoreDamage).setTooltipText("cover.item_filter.ignore_damage"));
        widgetGroup.accept(new ToggleButtonWidget(99, 0, 20, 20, GuiTextures.BUTTON_FILTER_NBT,
                () -> ignoreNBT, this::setIgnoreNBT).setTooltipText("cover.item_filter.ignore_nbt"));
    }

    @Override
    public @NotNull ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        SlotGroup filterInventory = new SlotGroup("filter_inv", 3, 1000, true);
        syncManager.registerSlotGroup(filterInventory);
        return ModularPanel.defaultPanel("simple_item_filter", 18 * 4, 18 * 3)
                .align(Alignment.Center)
                .child(new Row().coverChildren()
                        .align(Alignment.TopCenter).top(6)
                        .child(SlotGroupWidget.builder()
                                .matrix("XXX", "XXX", "XXX")
                                .key('X', index -> new ItemSlot()
                                        .slot(SyncHandlers.phantomItemSlot(this.itemFilterSlots, index)
                                                .slotGroup(filterInventory)))
                                .synced(filterInventory.getName())
                                .build())
                        .child(new CycleButtonWidget()
                                .value(new BoolValue.Dynamic(this::isBlacklistFilter, this::setBlacklistFilter))
                                .textureGetter(state -> state == 0 ? GTGuiTextures.BUTTON_CROSS : GTGuiTextures.BUTTON)
                                .tooltip(tooltip -> tooltip.addLine(IKey.lang("cover.filter.blacklist")))));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setTag("ItemFilter", itemFilterSlots.serializeNBT());
        tagCompound.setBoolean("IgnoreDamage", ignoreDamage);
        tagCompound.setBoolean("IgnoreNBT", ignoreNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.itemFilterSlots.deserializeNBT(tagCompound.getCompoundTag("ItemFilter"));
        this.ignoreDamage = tagCompound.getBoolean("IgnoreDamage");
        this.ignoreNBT = tagCompound.getBoolean("IgnoreNBT");
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
}
