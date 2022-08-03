package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class MapItemStackNBTIngredient extends MapItemStackIngredient {
    @Nullable
    protected NBTCondition condition = null;
    @Nullable
    protected NBTMatcher matcher = null;

    public MapItemStackNBTIngredient(ItemStack stack, @Nullable NBTMatcher matcher, @Nullable NBTCondition condition) {
        super(stack);
        this.matcher = matcher;
        this.condition = condition;
    }

    public MapItemStackNBTIngredient(ItemStack stack, int meta, NBTTagCompound tag) {
        super(stack, meta, tag);
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * meta;
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
        return "MapItemStackIngredient{" + "item=" + stack.getItem().getRegistryName() + "}" + "{meta=" + meta + "} {matcher=" + matcher + "}" + "{condition=" + condition + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
