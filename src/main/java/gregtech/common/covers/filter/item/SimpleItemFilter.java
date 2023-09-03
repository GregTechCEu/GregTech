package gregtech.common.covers.filter.item;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.util.LargeStackSizeItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class SimpleItemFilter extends ItemFilter {

    private static final int MAX_MATCH_SLOTS = 9;

    protected final ItemStackHandler itemFilterSlots;
    protected boolean ignoreDamage = true;
    protected boolean ignoreNBT = true;

    public SimpleItemFilter() {
        this.itemFilterSlots = new LargeStackSizeItemStackHandler(MAX_MATCH_SLOTS) {
            @Override
            public int getSlotLimit(int slot) {
                return getMaxStackSize();
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
    public Object matchItemStack(ItemStack itemStack) {
        int itemFilterMatchIndex = itemFilterMatch(getItemFilterSlots(), isIgnoreDamage(), isIgnoreNBT(), itemStack);
        return itemFilterMatchIndex == -1 ? null : itemFilterMatchIndex;
    }

    @Override
    public int getTransferLimit(Object obj, int globalTransferLimit) {
        if (obj instanceof Integer) {
            return (int) obj;
        }
        return 0;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public @NotNull IWidget createFilterUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        ParentWidget<?> parentWidget = new ParentWidget<>().coverChildren();
        parentWidget.child(new CycleButtonWidget()
                        .value(new BooleanSyncValue(() -> ignoreDamage, this::setIgnoreDamage))
                        .texture(gregtech.api.newgui.GuiTextures.BUTTON_FILTER_DAMAGE)
                        .addTooltip(0, IKey.lang("cover.item_filter.ignore_damage.disabled"))
                        .addTooltip(1, IKey.lang("cover.item_filter.ignore_damage.enabled"))
                        .pos(85, 0)
                        .size(18, 18))
                .child(new CycleButtonWidget()
                        .value(new BooleanSyncValue(() -> ignoreNBT, this::setIgnoreNBT))
                        .texture(gregtech.api.newgui.GuiTextures.BUTTON_FILTER_NBT)
                        .addTooltip(0, IKey.lang("cover.item_filter.ignore_nbt.disabled"))
                        .addTooltip(1, IKey.lang("cover.item_filter.ignore_nbt.enabled"))
                        .pos(103, 0)
                        .size(18, 18))
                .child(createBlacklistButton(mainPanel, syncManager));
        for (int i = 0; i < 9; i++) {
            parentWidget.child(new ItemSlot()
                    .slot(new ModularSlot(this.itemFilterSlots, i, true))
                    .pos(i % 3 * 18, i / 3 * 18));
        }
        return parentWidget;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("ItemFilter", itemFilterSlots.serializeNBT());
        tagCompound.setBoolean("IgnoreDamage", ignoreDamage);
        tagCompound.setBoolean("IgnoreNBT", ignoreNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.itemFilterSlots.deserializeNBT(tagCompound.getCompoundTag("ItemFilter"));
        this.ignoreDamage = tagCompound.getBoolean("IgnoreDamage");
        this.ignoreNBT = tagCompound.getBoolean("IgnoreNBT");
    }

    public static int itemFilterMatch(IItemHandler filterSlots, boolean ignoreDamage, boolean ignoreNBTData, ItemStack itemStack) {
        for (int i = 0; i < filterSlots.getSlots(); i++) {
            ItemStack filterStack = filterSlots.getStackInSlot(i);
            if (!filterStack.isEmpty() && areItemsEqual(ignoreDamage, ignoreNBTData, filterStack, itemStack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean areItemsEqual(boolean ignoreDamage, boolean ignoreNBTData, ItemStack filterStack, ItemStack itemStack) {
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
