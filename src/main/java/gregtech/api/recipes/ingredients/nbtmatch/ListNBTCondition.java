package gregtech.api.recipes.ingredients.nbtmatch;

import net.minecraft.nbt.NBTBase;

import java.util.List;

public class ListNBTCondition extends NBTCondition {

    public static ListNBTCondition create(NBTTagType listTagType, String nbtKey, List<NBTBase> value) {
        return new ListNBTCondition(listTagType, nbtKey, value);
    }

    public final NBTTagType listTagType;

    protected ListNBTCondition(NBTTagType listTagType, String nbtKey, Object value) {
        super(NBTTagType.LIST, nbtKey, value);
        this.listTagType = listTagType;
    }

    @Override
    public String toString() {
        return nbtKey + " (type " + listTagType + ") :" +  value;
    }

}
