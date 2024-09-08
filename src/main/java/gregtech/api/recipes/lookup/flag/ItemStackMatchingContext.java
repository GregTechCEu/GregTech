package gregtech.api.recipes.lookup.flag;

public enum ItemStackMatchingContext {
    ITEM, ITEM_DAMAGE, ITEM_NBT, ITEM_DAMAGE_NBT;

    public boolean matchesDamage() {
        return this == ITEM_DAMAGE || this == ITEM_DAMAGE_NBT;
    }

    public boolean matchesNBT() {
        return this == ITEM_NBT || this == ITEM_DAMAGE_NBT;
    }
}
