package gregtech.api.recipes.ingredients.nbtmatch;

import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTBase;

import java.util.List;
import java.util.Objects;

public class ListNBTCondition extends NBTCondition {

    public static ListNBTCondition create(NBTTagType listTagType, String nbtKey, List<NBTBase> value) {
        return new ListNBTCondition(listTagType, nbtKey, value);
    }

    public final NBTTagType listTagType;

    protected ListNBTCondition(NBTTagType listTagType, String nbtKey, Object value) {
        super(NBTTagType.LIST, nbtKey, value);
        this.listTagType = listTagType;
        if (listTagType == null) {
            GTLog.logger.error("ListNBTCondition must not have null parameters.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
        }
    }

    @Override
    public String toString() {
        return nbtKey + " (type " + listTagType + ") :" + value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagType, nbtKey, value, listTagType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ListNBTCondition) {
            ListNBTCondition o = (ListNBTCondition) obj;
            return this.tagType == o.tagType && this.nbtKey.equals(o.nbtKey) && this.value.equals(o.value) &&
                    this.listTagType == o.listTagType;
        }
        return false;
    }
}
