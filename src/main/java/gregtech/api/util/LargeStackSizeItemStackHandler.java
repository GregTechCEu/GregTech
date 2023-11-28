package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.ItemStackHandler;

/**
 * This is facade for ItemStackHandler which supports NBT handling of stack sizes > 127
 */
public class LargeStackSizeItemStackHandler extends ItemStackHandler {

    private static final String ITEM_LIST_TAG_KEY = "Items";
    private static final String ITEM_COUNT_TAG_KEY = "Count";
    private static final String BIG_STACK_SIZE_TAG_KEY = "BigStackSize";
    private static final Byte FAKE_STACK_SIZE = new Byte("1");

    public LargeStackSizeItemStackHandler(int maxMatchSlots) {
        super(maxMatchSlots);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = super.serializeNBT();

        if (stacks.stream().anyMatch(x -> x.getCount() > Byte.MAX_VALUE)) {
            NBTTagCompound stackSizes = new NBTTagCompound();
            NBTTagList items = tagCompound.getTagList(ITEM_LIST_TAG_KEY, 10);

            // save big stack size data
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack itemStack = stacks.get(i);

                if (itemStack != ItemStack.EMPTY && itemStack.getCount() > Byte.MAX_VALUE) {
                    stackSizes.setInteger(String.valueOf(i), itemStack.getCount());
                }
            }
            tagCompound.setTag(BIG_STACK_SIZE_TAG_KEY, stackSizes);

            // fix size overflow of existing item tags
            for (NBTBase itemBase : items) {
                NBTTagCompound item = (NBTTagCompound) itemBase;

                byte size = item.getByte(ITEM_COUNT_TAG_KEY);
                if (size < 0)
                    item.setByte(ITEM_COUNT_TAG_KEY, FAKE_STACK_SIZE);
            }
        }

        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        super.deserializeNBT(tagCompound);

        if (tagCompound.hasKey(BIG_STACK_SIZE_TAG_KEY)) {
            NBTTagCompound stackSizes = tagCompound.getCompoundTag(BIG_STACK_SIZE_TAG_KEY);

            for (String tagKey : stackSizes.getKeySet()) {
                int size = stackSizes.getInteger(tagKey);
                int slot = Integer.parseInt(tagKey);
                stacks.get(slot).setCount(size);
            }
        }
    }
}
