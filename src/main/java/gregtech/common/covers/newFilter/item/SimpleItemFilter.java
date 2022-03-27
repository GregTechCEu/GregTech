package gregtech.common.covers.newFilter.item;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.LargeStackSizeItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Set;

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
    public Integer matchItemStack(ItemStack itemStack) {
        int itemFilterMatchIndex = itemFilterMatch(getItemFilterSlots(), isIgnoreDamage(), isIgnoreNBT(), itemStack);
        return itemFilterMatchIndex == -1 ? null : itemFilterMatchIndex;
    }

    @Override
    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
        Integer matchSlotIndex = (Integer) matchSlot;
        ItemStack stackInFilterSlot = itemFilterSlots.getStackInSlot(matchSlotIndex);
        return Math.min(stackInFilterSlot.getCount(), globalTransferLimit);
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return false;
    }

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        MultiChildWidget widget = new MultiChildWidget();
        widget
                .addChild(new CycleButtonWidget()
                        .setToggle(() -> ignoreDamage, this::setIgnoreDamage)
                        .setTexture(GuiTextures.BUTTON_FILTER_DAMAGE)
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .addTooltip(new Text("cover.item_filter.ignore_damage").localise())
                        .setPos(90, 0)
                        .setSize(18, 18))
                .addChild(new CycleButtonWidget()
                        .setToggle(() -> ignoreNBT, this::setIgnoreNBT)
                        .setTexture(GuiTextures.BUTTON_FILTER_NBT)
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .addTooltip(new Text("cover.item_filter.ignore_nbt").localise())
                        .setPos(108, 0)
                        .setSize(18, 18))
                .addChild(createBlacklistButton(buildContext));
        for (int i = 0; i < 9; i++) {
            widget.addChild(SlotWidget.phantom(itemFilterSlots, i)
                    .setPos(i * 18, 19));
        }
        return widget;
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
