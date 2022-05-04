package gregtech.api.recipes.map;

import gregtech.api.recipes.CountableIngredient;
import net.minecraft.item.ItemStack;

public class MapItemStackNBTIngredient extends AbstractMapIngredient {

    public final ItemStack stack;
    public final CountableIngredient.NBTcondition condition;

    public MapItemStackNBTIngredient(ItemStack stack, CountableIngredient.NBTcondition condition) {
        this.stack = stack;
        this.condition = condition;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * stack.getMetadata();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapItemStackIngredient)) {
            return false;
        }
        MapItemStackIngredient other = (MapItemStackIngredient) o;
        return ItemStack.areItemsEqual(stack, other.stack) && condition.evaluate(other.stack);
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + '}';
    }

    @Override
    public boolean conditionalNBT() {
        return true;
    }
}
