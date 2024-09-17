package gregtech.api.recipes.lookup.flag;

import net.minecraft.item.ItemStack;

public enum ItemStackMatchingContext {

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

    public boolean matches(ItemStack a, ItemStack b) {
        return switch (this) {
            case ITEM -> ItemStackApplicatorMap.ITEM.equals(a, b);
            case ITEM_DAMAGE -> ItemStackApplicatorMap.ITEM_DAMAGE.equals(a, b);
            case ITEM_NBT -> ItemStackApplicatorMap.ITEM_NBT.equals(a, b);
            case ITEM_DAMAGE_NBT -> ItemStackApplicatorMap.ITEM_DAMAGE_NBT.equals(a, b);
        };
    }
}
