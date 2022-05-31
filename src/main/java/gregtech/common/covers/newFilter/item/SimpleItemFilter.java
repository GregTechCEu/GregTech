package gregtech.common.covers.newFilter.item;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.CycleButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.LargeStackSizeItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public Widget createFilterUI(EntityPlayer player) {
        MultiChildWidget widget = new MultiChildWidget();
        widget
                .addChild(new CycleButtonWidget()
                        .setToggle(() -> ignoreDamage, this::setIgnoreDamage)
                        .setTexture(GuiTextures.BUTTON_FILTER_DAMAGE)
                        .addTooltip(0, Text.localised("cover.item_filter.ignore_damage.disabled"))
                        .addTooltip(1, Text.localised("cover.item_filter.ignore_damage.enabled"))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setPos(85, 0)
                        .setSize(18, 18))
                .addChild(new CycleButtonWidget()
                        .setToggle(() -> ignoreNBT, this::setIgnoreNBT)
                        .setTexture(GuiTextures.BUTTON_FILTER_NBT)
                        .addTooltip(0, Text.localised("cover.item_filter.ignore_nbt.disabled"))
                        .addTooltip(1, Text.localised("cover.item_filter.ignore_nbt.enabled"))
                        .setBackground(GuiTextures.BASE_BUTTON)
                        .setPos(103, 0)
                        .setSize(18, 18))
                .addChild(createBlacklistButton(player));
        for (int i = 0; i < 9; i++) {
            widget.addChild(SlotWidget.phantom(itemFilterSlots, i)
                    .setPos(i % 3 * 18, i / 3 * 18));
        }
        return widget.setSize(140, 54);
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
