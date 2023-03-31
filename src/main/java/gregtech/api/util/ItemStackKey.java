package gregtech.api.util;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * ItemStackKey implementation intended to be used
 * as a key in hash maps for itemstack comparision reasons
 * Objects of ItemStackKey are equal only if their contained
 * ItemStacks are equal (excluding stack size)
 *
 * @deprecated Use {@link ItemStackHashStrategy#comparingAllButCount()} with Custom FastUtil maps instead
 */
@Deprecated
public final class ItemStackKey {

    private final ItemStack itemStack;
    private final int maxStackSize;
    private final int hashCode;

    public ItemStackKey(@Nonnull ItemStack itemStack) {
        this.itemStack = itemStack.copy();
        this.itemStack.setCount(1);
        this.hashCode = makeHashCode();
        this.maxStackSize = itemStack.getMaxStackSize();
    }

    public ItemStackKey(@Nonnull ItemStack itemStack, boolean doCopy) {
        this.itemStack = itemStack;
        this.maxStackSize = itemStack.getMaxStackSize();
        this.hashCode = makeHashCode();
    }

    public boolean isItemStackEqual(ItemStack itemStack) {
        return ItemStackHashStrategy.comparingAllButCount().equals(this.itemStack, itemStack);
    }

    @Nonnull
    public ItemStack getItemStack() {
        return itemStack.copy();
    }

    @Nonnull
    public ItemStack getItemStackRaw() {
        return itemStack;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStackKey)) return false;
        ItemStackKey that = (ItemStackKey) o;
        return ItemStack.areItemsEqual(itemStack, that.itemStack) &&
                ItemStack.areItemStackTagsEqual(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int makeHashCode() {
        return ItemStackHashStrategy.comparingAllButCount().hashCode(itemStack);
    }

    @Nonnull
    @Override
    public String toString() {
        return itemStack.toString();
    }
}
