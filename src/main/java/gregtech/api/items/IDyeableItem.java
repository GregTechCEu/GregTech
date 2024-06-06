package gregtech.api.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Note - users of this interface will have to deal with cauldron dye clearing on their own.
 */
public interface IDyeableItem {

    default boolean hasColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        return nbttagcompound != null && nbttagcompound.hasKey("display", 10) &&
                nbttagcompound.getCompoundTag("display").hasKey("color", 3);
    }

    default int getColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
            if (nbttagcompound1.hasKey("color", 3)) {
                return nbttagcompound1.getInteger("color");
            }
        }
        return getDefaultColor(stack);
    }

    default int getDefaultColor(ItemStack stack) {
        return 0xFFFFFF;
    }

    default void removeColor(ItemStack stack) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
            if (nbttagcompound1.hasKey("color")) {
                nbttagcompound1.removeTag("color");
            }
        }
    }

    default void setColor(ItemStack stack, int color) {
        NBTTagCompound nbttagcompound = stack.getTagCompound();
        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
        }
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
        if (!nbttagcompound.hasKey("display", 10)) {
            nbttagcompound.setTag("display", nbttagcompound1);
        }
        nbttagcompound1.setInteger("color", color);
    }

    /**
     * Controls whether the dyeing recipe simply removes the dyeable item from the crafting grid,
     * or calls {@link net.minecraftforge.common.ForgeHooks#getContainerItem(ItemStack)} on it.
     */
    default boolean shouldGetContainerItem() {
        return true;
    }
}
