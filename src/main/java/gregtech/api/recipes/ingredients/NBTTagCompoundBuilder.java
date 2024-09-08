package gregtech.api.recipes.ingredients;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class NBTTagCompoundBuilder {

    private final NBTTagCompound building = new NBTTagCompound();

    public NBTTagCompoundBuilder setTag(String key, NBTBase value) {
        building.setTag(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setByte(String key, byte value) {
        building.setByte(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setShort(String key, short value) {
        building.setShort(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setInteger(String key, int value) {
        building.setInteger(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setLong(String key, long value) {
        building.setLong(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setFloat(String key, float value) {
        building.setFloat(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setDouble(String key, double value) {
        building.setDouble(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setString(String key, String value) {
        building.setString(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setByteArray(String key, byte[] value) {
        building.setByteArray(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setIntArray(String key, int[] value) {
        building.setIntArray(key, value);
        return this;
    }

    public NBTTagCompoundBuilder setBoolean(String key, boolean value) {
        building.setBoolean(key, value);
        return this;
    }

    public NBTTagCompound build() {
        return building;
    }
}
