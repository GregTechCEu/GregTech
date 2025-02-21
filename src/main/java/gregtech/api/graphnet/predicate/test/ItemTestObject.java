package gregtech.api.graphnet.predicate.test;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public class ItemTestObject implements IPredicateTestObject, Predicate<ItemStack> {

    public final Item item;
    public final int meta;
    public final @Nullable NBTTagCompound tag;

    public final int stackLimit;

    private int hash = Integer.MIN_VALUE;

    public ItemTestObject(@NotNull ItemStack stack) {
        item = stack.getItem();
        meta = stack.getMetadata();
        tag = stack.getTagCompound();
        stackLimit = stack.getMaxStackSize();
    }

    @Override
    @Contract(" -> new")
    public ItemStack recombine() {
        ItemStack stack = new ItemStack(item, 1, meta);
        if (tag != null) stack.setTagCompound(tag.copy());
        return stack;
    }

    @Contract("_ -> new")
    public ItemStack recombineSafe(int amount) {
        return recombine(Math.min(getStackLimit(), Math.max(0, amount)));
    }

    @Contract("_ -> new")
    public ItemStack recombine(int amount) {
        assert amount <= getStackLimit() && amount > 0;
        ItemStack stack = new ItemStack(item, amount, meta);
        if (tag != null) stack.setTagCompound(tag.copy());
        return stack;
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.stackLimit == stack.getMaxStackSize() && this.item == stack.getItem() &&
                this.meta == stack.getMetadata()) {
            NBTTagCompound other = stack.getTagCompound();
            return Objects.equals(this.tag, other);
        }
        return false;
    }

    public int getStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemTestObject that = (ItemTestObject) o;
        return meta == that.meta && Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        if (hash == Integer.MIN_VALUE) {
            hash = Objects.hash(item, meta, tag);
        }
        return hash;
    }
}
