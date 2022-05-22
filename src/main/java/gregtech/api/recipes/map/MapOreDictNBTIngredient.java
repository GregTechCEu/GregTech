package gregtech.api.recipes.map;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class MapOreDictNBTIngredient extends MapOreDictIngredient {

    @Nullable
    protected NBTcondition condition = null;
    @Nullable
    protected NBTMatcher matcher = null;
    @Nullable
    protected NBTTagCompound nbtTagCompound = null;

    public MapOreDictNBTIngredient(int ore, @Nullable NBTMatcher matcher, @Nullable NBTcondition condition) {
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
