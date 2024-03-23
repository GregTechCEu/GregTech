package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.jetbrains.annotations.NotNull;

public interface FilterReader {

    ItemStack getContainer();

    void readStack(ItemStack stack);

    @NotNull
    NBTTagList getInventoryNbt();

    @NotNull
    NBTTagCompound getStackTag();

    int getSize();

    boolean validateSlotIndex(int slot);

    @NotNull
    default NBTTagCompound getTagAt(int i) {
        if (validateSlotIndex(i)) {
            return getInventoryNbt().getCompoundTagAt(i);
        }
        return new NBTTagCompound();
    }
}
