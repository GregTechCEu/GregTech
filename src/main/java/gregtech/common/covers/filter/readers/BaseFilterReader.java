package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.utils.ItemStackItemHandler;

public abstract class BaseFilterReader extends ItemStackItemHandler {

    protected final ItemStack container;
    protected static final String KEY_ITEMS = "Items";
    protected static final String BLACKLIST = "is_blacklist";
    public BaseFilterReader(ItemStack container, int slots) {
        super(container, slots);
        this.container = container;
        setBlacklistFilter(false);
    }

    public ItemStack getContainer () {
        return this.container;
    }

    public abstract void onMaxStackSizeChange();

    public final void setBlacklistFilter(boolean blacklistFilter) {
        getStackTag().setBoolean(BLACKLIST, blacklistFilter);
        onMaxStackSizeChange();
    }

    public final boolean isBlacklistFilter() {
        return getStackTag().getBoolean(BLACKLIST);
    }

    protected NBTTagCompound getStackTag() {
        if (!container.hasTagCompound()) {
            container.setTagCompound(new NBTTagCompound());
        }
        return container.getTagCompound();
    }

    @Override
    public NBTTagList getItemsNbt() {
        NBTTagCompound nbt = getStackTag();
        if (!nbt.hasKey(KEY_ITEMS)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSlots(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(KEY_ITEMS, list);
        }
        return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    @Override
    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.getSlots()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
        }
    }
}
