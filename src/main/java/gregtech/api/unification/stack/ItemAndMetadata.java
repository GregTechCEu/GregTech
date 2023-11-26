package gregtech.api.unification.stack;

import gregtech.api.GTValues;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public final class ItemAndMetadata {

    @Nonnull
    public final Item item;
    public final int itemDamage;

    public ItemAndMetadata(@Nonnull Item item, int itemDamage) {
        this.item = item;
        this.itemDamage = itemDamage;
    }

    public ItemAndMetadata(@Nonnull ItemStack itemStack) {
        this.item = itemStack.getItem();
        this.itemDamage = itemStack.getItemDamage();
    }

    @Nonnull
    public ItemStack toItemStack() {
        return new ItemStack(item, 1, itemDamage);
    }

    @Nonnull
    public ItemStack toItemStack(int stackSize) {
        return new ItemStack(item, stackSize, itemDamage);
    }

    public boolean isWildcard() {
        return this.itemDamage == GTValues.W;
    }

    @Nonnull
    public ItemAndMetadata toWildcard() {
        return this.isWildcard() ? this : new ItemAndMetadata(item, GTValues.W);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemAndMetadata)) return false;

        ItemAndMetadata that = (ItemAndMetadata) o;

        if (itemDamage != that.itemDamage) return false;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + itemDamage;
        return result;
    }

    @Override
    public String toString() {
        return this.item.getTranslationKey(toItemStack());
    }
}
