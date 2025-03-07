package gregtech.api.recipes.lookup.flag;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.Hash;

public enum ItemStackMatchingContext implements Hash.Strategy<ItemStack> {

    ITEM,
    ITEM_DAMAGE,
    ITEM_NBT,
    ITEM_DAMAGE_NBT;

    public static final ItemStackMatchingContext[] VALUES = values();

    public boolean matchesDamage() {
        return this == ITEM_DAMAGE || this == ITEM_DAMAGE_NBT;
    }

    public boolean matchesNBT() {
        return this == ITEM_NBT || this == ITEM_DAMAGE_NBT;
    }

    @Override
    public boolean equals(ItemStack a, ItemStack b) {
        return switch (this) {
            case ITEM -> ItemStackApplicatorMap.ITEM.equals(a, b);
            case ITEM_DAMAGE -> ItemStackApplicatorMap.ITEM_DAMAGE.equals(a, b);
            case ITEM_NBT -> ItemStackApplicatorMap.ITEM_NBT.equals(a, b);
            case ITEM_DAMAGE_NBT -> ItemStackApplicatorMap.ITEM_DAMAGE_NBT.equals(a, b);
        };
    }

    @Override
    public int hashCode(ItemStack o) {
        return switch (this) {
            case ITEM -> ItemStackApplicatorMap.ITEM.hashCode(o);
            case ITEM_DAMAGE -> ItemStackApplicatorMap.ITEM_DAMAGE.hashCode(o);
            case ITEM_NBT -> ItemStackApplicatorMap.ITEM_NBT.hashCode(o);
            case ITEM_DAMAGE_NBT -> ItemStackApplicatorMap.ITEM_DAMAGE_NBT.hashCode(o);
        };
    }
}
