package gregtech.api.recipes.map;

import net.minecraft.item.ItemStack;

public class MapItemStackIngredient extends AbstractMapIngredient {

    protected ItemStack stack;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return ItemStack.areItemsEqual(stack, other.stack) && ItemStack.areItemStackTagsEqual(stack, other.stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * stack.getMetadata();
        hash += 31 * (stack.getTagCompound() != null ? stack.getTagCompound().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + '}';
    }

    @Override
    public boolean isSpecialIngredient() {
        return false;
    }
}
