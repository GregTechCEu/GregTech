package gregtech.api.util.function;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

@FunctionalInterface
public interface NBTTagCompoundSupplier {

    NBTTagCompound getTag();

    default boolean hasKey(String key) {
        return getTag().hasKey(key);
    }

    default boolean hasKey(String key, int type) {
        return getTag().hasKey(key, type);
    }

    default void setTag(String key, NBTBase value) {
        getTag().setTag(key, value);
    }

    default void getTag(String key) {
        getTag().getTag(key);
    }

    default NBTTagList getTagList(String key, int type) {
        return getTag().getTagList(key, type);
    }

    default void getCompoundTag(String key) {
        getTag().getCompoundTag(key);
    }

    default byte getTagID(String key) {
        return getTag().getTagId(key);
    }

    default void removeTag(String key) {
        getTag().removeTag(key);
    }

    default boolean isEmpty() {
        return getTag().isEmpty();
    }

    default void merge(NBTTagCompound other) {
        getTag().merge(other);
    }

    default void setBoolean(String key, boolean value) {
        getTag().setBoolean(key, value);
    }

    default boolean getBoolean(String key) {
        return getTag().getBoolean(key);
    }

    default void setByte(String key, byte value) {
        getTag().setByte(key, value);
    }

    default byte getByte(String key) {
        return getTag().getByte(key);
    }

    default void setShort(String key, short value) {
        getTag().setShort(key, value);
    }

    default short getShort(String key) {
        return getTag().getShort(key);
    }

    default void setInteger(String key, int value) {
        getTag().setInteger(key, value);
    }

    default int getInteger(String key) {
        return getTag().getInteger(key);
    }

    default void setLong(String key, long value) {
        getTag().setLong(key, value);
    }

    default long getLong(String key) {
        return getTag().getLong(key);
    }

    default void setFloat(String key, float value) {
        getTag().setFloat(key, value);
    }

    default float getFloat(String key) {
        return getTag().getFloat(key);
    }

    default void setDouble(String key, double value) {
        getTag().setDouble(key, value);
    }

    default double getDouble(String key) {
        return getTag().getDouble(key);
    }

    default void setString(String key, String value) {
        getTag().setString(key, value);
    }

    default String getString(String key) {
        return getTag().getString(key);
    }

    default void setByteArray(String key, byte[] value) {
        getTag().setByteArray(key, value);
    }

    default byte[] getByteArray(String key) {
        return getTag().getByteArray(key);
    }

    default void setIntArray(String key, int[] value) {
        getTag().setIntArray(key, value);
    }

    default int[] getIntArray(String key) {
        return getTag().getIntArray(key);
    }
}
