package gregtech.api.unification.stack;

import gregtech.api.GTValues;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public final class ItemAndMetadata {

    @NotNull
    public final Item item;
    public final int itemDamage;

    public ItemAndMetadata(@NotNull Item item, int itemDamage) {
        this.item = item;
        this.itemDamage = itemDamage;
    }

    public ItemAndMetadata(@NotNull ItemStack itemStack) {
        this.item = itemStack.getItem();
        this.itemDamage = itemStack.getItemDamage();
    }

    @NotNull
    public ItemStack toItemStack() {
        return new ItemStack(item, 1, itemDamage);
    }

    @NotNull
    public ItemStack toItemStack(int stackSize) {
        return new ItemStack(item, stackSize, itemDamage);
    }

    public boolean isWildcard() {
        return this.itemDamage == GTValues.W;
    }

    @NotNull
    public ItemAndMetadata toWildcard() {
        return this.isWildcard() ? this : new ItemAndMetadata(item, GTValues.W);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ItemAndMetadata that && itemDamage == that.itemDamage && item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return 31 * item.hashCode() + itemDamage;
    }

    @Override
    public String toString() {
        return this.item.getTranslationKey(toItemStack());
    }
}
