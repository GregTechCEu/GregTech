package gregtech.api.recipes.map;

import net.minecraft.item.ItemStack;

import java.util.Objects;

public class MapItemStackNBTIngredient extends AbstractMapIngredient {

    public final ItemStack stack;

    public MapItemStackNBTIngredient(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * stack.getMetadata();
        hash += 31 * Objects.hash(stack.getTagCompound());
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapItemStackIngredient)) {
            return false;
        }
        MapItemStackIngredient other = (MapItemStackIngredient) o;
        return stack.isItemEqual(other.stack);
    }

    @Override
    public Object getComparableIngredient() {
        return stack;
    }

    @Override
    public boolean matchesNBT(Object obj) {
        if (obj instanceof ItemStack) {
            ItemStack other = (ItemStack) obj;
            return ItemStack.areItemStackTagsEqual(stack, other);
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" +
                "item=" + stack.getItem().getRegistryName() +
                '}';
    }

    @Override
    public boolean NBTsensitive() {
        return true;
    }
}
