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
        if (super.equals(o)) {
            MapItemStackNBTIngredient other = (MapItemStackNBTIngredient) o;
            //NBT condition is only avaliable on the MapItemStackNBTIngredient created by from the Recipe, so
            //the evaluate method is called from the comparing MapItemStackNBTIngredient that is on the RecipeMap
            return ItemStack.areItemsEqual(stack, other.stack) && other.condition.evaluate(this.stack);
        }
        return false;
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
