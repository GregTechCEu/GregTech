package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class MapItemStackNBTIngredient extends AbstractMapIngredient {

    public final ItemStack stack;
    @Nullable
    public final NBTcondition condition;
    @Nullable
    public final NBTMatcher matcher;

    public MapItemStackNBTIngredient(ItemStack stack, @Nullable NBTMatcher matcher, @Nullable NBTcondition condition) {
        this.stack = stack;
        this.matcher = matcher;
        this.condition = condition;
    }

    public MapItemStackNBTIngredient(ItemStack stack) {
        this.stack = stack;
        this.matcher = null;
        this.condition = null;
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
            //NBT condition is only available on the MapItemStackNBTIngredient created by from the Recipe, so
            //the evaluate method is called from the comparing MapItemStackNBTIngredient that is on the RecipeMap
            return ItemStack.areItemsEqual(stack, other.stack) && other.matcher.evaluate(this.stack, other.condition);
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
