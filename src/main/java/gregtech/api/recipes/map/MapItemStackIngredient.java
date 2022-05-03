package gregtech.api.recipes.map;

import net.minecraft.item.ItemStack;

import java.util.Objects;

public class MapItemStackIngredient extends AbstractMapIngredient {

    public final ItemStack stack;

    public MapItemStackIngredient(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return ItemStack.areItemsEqual(stack, other.stack);
        }
        return false;
    }

    @Override
    public Object getComparableIngredient() {
        return stack;
    }

    @Override
    public boolean matchesNBT(Object obj) {
        return obj instanceof ItemStack;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * stack.getMetadata();
        return hash;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" +
                "item=" + stack.getItem().getRegistryName() +
                '}';
    }



}
