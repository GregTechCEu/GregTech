package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

public interface FilterReader {
    ItemStack getContainer();

    @NotNull
    default NBTTagList getInventoryNbt() {
        var nbt = getStackTag();
        String key = getKey();
        if (!nbt.hasKey(key)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSize(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(key, list);
        }
        return nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
    }

    @NotNull
    default NBTTagCompound getStackTag() {
        NBTTagCompound nbt = getContainer().getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            getContainer().setTagCompound(nbt);
        }
        return nbt;
    }

    String getKey();

    int getSize();

    boolean validateSlotIndex(int slot);

    @NotNull
    default NBTTagCompound getTagAt(int i ) {
        if (validateSlotIndex(i)) {
            return getInventoryNbt().getCompoundTagAt(i);
        }
        return new NBTTagCompound();
    }
}
