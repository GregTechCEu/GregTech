package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

public class MapOreDictNBTIngredient extends MapOreDictIngredient {

    @Nullable
    protected NBTCondition condition = null;
    @Nullable
    protected NBTMatcher matcher = null;
    @Nullable
    protected NBTTagCompound nbtTagCompound = null;

    public MapOreDictNBTIngredient(int ore, @Nullable NBTMatcher matcher, @Nullable NBTCondition condition) {
        super(ore);
        this.matcher = matcher;
        this.condition = condition;
    }

    public MapOreDictNBTIngredient(int ore, @Nullable NBTTagCompound nbtTagCompound) {
        super(ore);
        this.nbtTagCompound = nbtTagCompound;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MapOreDictNBTIngredient) {
            MapOreDictNBTIngredient other = (MapOreDictNBTIngredient) obj;
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
            if (ore == other.ore) {
                return other.matcher.evaluate(this.nbtTagCompound, other.condition);
            }
        }
        return false;
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
