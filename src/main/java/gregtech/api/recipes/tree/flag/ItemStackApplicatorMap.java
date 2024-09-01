package gregtech.api.recipes.tree.flag;

import it.unimi.dsi.fastutil.Hash;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemStackApplicatorMap extends Object2ApplicatorMapMap<ItemStack> {

    @Contract(" -> new")
    public static @NotNull ItemStackApplicatorMap item() {
        return new ItemStackApplicatorMap(ITEM);
    }

    @Contract(" -> new")
    public static @NotNull ItemStackApplicatorMap itemDamage() {
        return new ItemStackApplicatorMap(ITEM_DAMAGE);
    }

    @Contract(" -> new")
    public static @NotNull ItemStackApplicatorMap itemNBT() {
        return new ItemStackApplicatorMap(ITEM_NBT);
    }

    @Contract(" -> new")
    public static @NotNull ItemStackApplicatorMap itemDamageNBT() {
        return new ItemStackApplicatorMap(ITEM_DAMAGE_NBT);
    }

    public ItemStackApplicatorMap(Hash.Strategy<ItemStack> strategy) {
        super(strategy);
    }

    public static final Hash.Strategy<ItemStack> ITEM = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            return o.getItem().hashCode();
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) return false;
            return a.getItem().equals(b.getItem());
        }
    };

    public static final Hash.Strategy<ItemStack> ITEM_DAMAGE = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            return 97 * o.getItem().hashCode() + 31 * o.getItemDamage();
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) return false;
            return a.getItem().equals(b.getItem()) && a.getItemDamage() == b.getItemDamage();
        }
    };

    public static final Hash.Strategy<ItemStack> ITEM_NBT = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            return 97 * o.getItem().hashCode() + hashNBT(o.getTagCompound());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) return false;
            return a.getItem().equals(b.getItem()) && Objects.equals(a.getTagCompound(), b.getTagCompound());
        }
    };

    public static final Hash.Strategy<ItemStack> ITEM_DAMAGE_NBT = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            return 97 * o.getItem().hashCode() + 31 * o.getItemDamage() + hashNBT(o.getTagCompound());
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) return false;
            return a.getItem().equals(b.getItem()) && a.getItemDamage() == b.getItemDamage() && Objects.equals(a.getTagCompound(), b.getTagCompound());
        }
    };

    static int hashNBT(@Nullable NBTTagCompound tag) {
        return tag == null ? 0 : 7919 * tag.hashCode();
    }
}
