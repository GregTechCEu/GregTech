package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class MapItemStackNBTIngredient extends MapItemStackIngredient {
    @Nullable
    protected NBTcondition condition = null;
    @Nullable
    protected NBTMatcher matcher = null;

    public MapItemStackNBTIngredient(ItemStack stack, @Nullable NBTMatcher matcher, @Nullable NBTcondition condition) {
        super(stack);
        this.matcher = matcher;
        this.condition = condition;
    }

    public MapItemStackNBTIngredient(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * stack.getMetadata();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapItemStackNBTIngredient) {
            MapItemStackNBTIngredient other = (MapItemStackNBTIngredient) obj;
            if (this.matcher != null && other.matcher != null) {
                if (!this.matcher.equals(other.matcher)) {
                    return false;
                }
            }
            if (this.condition != null && other.condition != null) {
                if (!this.condition.equals(other.condition)) {
                    return false;
                }
            }
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
    public boolean isSpecialIngredient() {
        return true;
    }
}
