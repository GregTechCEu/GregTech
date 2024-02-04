package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import com.cleanroommc.modularui.utils.ItemStackItemHandler;

public abstract class BaseFilterReader extends ItemStackItemHandler {

    protected final ItemStack container;
    protected int maxTransferRate = 1;
    protected static final String KEY_ITEMS = "ItemFilter";
    protected static final String BLACKLIST = "IsBlacklist";

    public BaseFilterReader(ItemStack container, int slots) {
        super(container, slots);
        this.container = container;
    }

    public ItemStack getContainer() {
        return this.container;
    }

    public abstract void onTransferRateChange();

    public final void setBlacklistFilter(boolean blacklistFilter) {
        setWhitelist(!blacklistFilter);
    }

    public final boolean isBlacklistFilter() {
        return getStackTag().getBoolean(BLACKLIST);
    }

    private void setWhitelist(boolean whitelist) {
        if (getStackTag().getBoolean(BLACKLIST) == whitelist) {
            if (whitelist)
                getStackTag().removeTag(BLACKLIST);
            else
                getStackTag().setBoolean(BLACKLIST, true);
            onTransferRateChange();
        }
    }

    public void setMaxTransferRate(int transferRate) {
        transferRate = MathHelper.clamp(transferRate, 1, Integer.MAX_VALUE);
        if (this.maxTransferRate != transferRate) {
            this.maxTransferRate = transferRate;
            onTransferRateChange();
        }
    }

    public int getMaxTransferRate() {
        return isBlacklistFilter() ? 1 : this.maxTransferRate;
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

    public void readFromNBT(NBTTagCompound tagCompound) {
        setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
    }
}
