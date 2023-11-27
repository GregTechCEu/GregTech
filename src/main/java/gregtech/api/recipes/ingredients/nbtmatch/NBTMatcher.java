package gregtech.api.recipes.ingredients.nbtmatch;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class is used to match NBT tags. Used to match a MapItemStackNBTIngredient NBT tag to a given NBT tag value.
 */
@SuppressWarnings("unused")
public interface NBTMatcher {

    static boolean hasKey(NBTTagCompound tag, String key, int tagType) {
        if (tag != null) {
            return tag.hasKey(key, tagType);
        }
        return false;
    }

    /**
     * Return true without checking if the NBT actually tags match or exists.
     */
    NBTMatcher ANY = (tag, condition) -> true;

    /**
     * Return true if tag has an entry where the value is less than the condition's value
     */
    NBTMatcher LESS_THAN = (tag, condition) -> {
        if (condition == null || condition.tagType == null) {
            return false;
        }
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (NBTTagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) < (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is less than or equal to the condition's value
     */
    NBTMatcher LESS_THAN_OR_EQUAL_TO = (tag, condition) -> {
        if (condition == null || condition.tagType == null) {
            return false;
        }
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (NBTTagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) <= (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is greater than the condition's value
     */
    NBTMatcher GREATER_THAN = (tag, condition) -> {
        if (condition == null || condition.tagType == null) {
            return false;
        }
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (NBTTagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) > (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is greater than or equal to the condition's value
     */
    NBTMatcher GREATER_THAN_OR_EQUAL_TO = (tag, condition) -> {
        if (condition == null || condition.tagType == null) {
            return false;
        }
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (NBTTagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) >= (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is equal to the condition's value
     */
    NBTMatcher EQUAL_TO = (tag, condition) -> {
        if (condition == null || condition.tagType == null) {
            return false;
        }
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (NBTTagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) == (long) condition.value;
            }
            switch (condition.tagType) {
                case BOOLEAN:
                    return tag.getBoolean(condition.nbtKey) == (boolean) condition.value;
                case BYTE_ARRAY:
                    return tag.getByteArray(condition.nbtKey).equals(condition.value);
                case STRING:
                    return tag.getString(condition.nbtKey).equals(condition.value);
                case LIST:
                    if (condition instanceof ListNBTCondition) {
                        return tag.getTagList(condition.nbtKey,
                                ((ListNBTCondition) condition).listTagType.typeId).tagList.equals(condition.value);
                    } else {
                        return false;
                    }
                case COMPOUND:
                    return tag.getCompoundTag(condition.nbtKey).equals(condition.value);
                case INT_ARRAY:
                    return tag.getIntArray(condition.nbtKey).equals(condition.value);
                case LONG_ARRAY:
                    return ((NBTTagLongArray) tag.getTag(condition.nbtKey)).data.equals(condition.value);
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is equal to the condition's value.
     * If NBTTagCompound is found, evaluates recursively.
     */
    NBTMatcher RECURSIVE_EQUAL_TO = new NBTMatcher() {

        @Override
        public boolean evaluate(@Nullable NBTTagCompound tag, @Nullable NBTCondition condition) {
            if (condition == null || condition.tagType == null) {
                return false;
            }
            if (NBTMatcher.hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
                if (NBTTagType.isNumeric(condition.tagType)) {
                    return Objects.equals(tag.getLong(condition.nbtKey), ((Number) condition.value).longValue());
                }
                switch (condition.tagType) {
                    case BOOLEAN:
                        return tag.getBoolean(condition.nbtKey) == (boolean) condition.value;
                    case BYTE_ARRAY:
                        return tag.getByteArray(condition.nbtKey).equals(condition.value);
                    case STRING:
                        return tag.getString(condition.nbtKey).equals(condition.value);
                    case LIST:
                        if (condition instanceof ListNBTCondition) {
                            return tag.getTagList(condition.nbtKey,
                                    ((ListNBTCondition) condition).listTagType.typeId).tagList.equals(condition.value);
                        } else {
                            return false;
                        }
                    case COMPOUND:
                        if (condition.value instanceof NBTCondition) {
                            return evaluate(tag.getCompoundTag(condition.nbtKey), (NBTCondition) condition.value);
                        } else {
                            return tag.getCompoundTag(condition.nbtKey).equals(condition.value);
                        }
                    case INT_ARRAY:
                        return tag.getIntArray(condition.nbtKey).equals(condition.value);
                    case LONG_ARRAY:
                        return ((NBTTagLongArray) tag.getTag(condition.nbtKey)).data.equals(condition.value);
                }
            }
            return false;
        }
    };

    /**
     * Return true if NBT isn't present or the value matches with the default value in the tag.
     */
    NBTMatcher NOT_PRESENT_OR_DEFAULT = (tag, condition) -> {
        if (tag == null || condition == null || condition.tagType == null) {
            return true;
        }
        if (NBTTagType.isNumeric(condition.tagType)) {
            return tag.getLong(condition.nbtKey) == 0;
        }
        switch (condition.tagType) {
            case BOOLEAN:
                return !tag.getBoolean(condition.nbtKey);
            case BYTE_ARRAY:
                return tag.getByteArray(condition.nbtKey).length == 0;
            case STRING:
                return tag.getString(condition.nbtKey).isEmpty();
            case LIST:
                if (condition instanceof ListNBTCondition) {
                    return tag.getTagList(condition.nbtKey, ((ListNBTCondition) condition).listTagType.typeId)
                            .isEmpty();
                } else {
                    return false;
                }
            case COMPOUND:
                return tag.getCompoundTag(condition.nbtKey).isEmpty();
            case INT_ARRAY:
                return tag.getIntArray(condition.nbtKey).length == 0;
            case LONG_ARRAY:
                return ((NBTTagLongArray) tag.getTag(condition.nbtKey)).data.length == 0;
        }
        return false;
    };

    /**
     * Return true if NBT isn't present or is the provided key is present
     */
    NBTMatcher NOT_PRESENT_OR_HAS_KEY = (tag, condition) -> {
        if (tag == null || condition == null || condition.tagType == null) {
            return true;
        }

        return hasKey(tag, condition.nbtKey, condition.tagType.typeId);
    };

    boolean evaluate(@Nullable NBTTagCompound nbtTagCompound, @Nullable NBTCondition nbtCondition);

    default boolean evaluate(@NotNull ItemStack stack, @Nullable NBTCondition nbtCondition) {
        return evaluate(stack.getTagCompound(), nbtCondition);
    }

    default boolean evaluate(@NotNull FluidStack stack, @Nullable NBTCondition nbtCondition) {
        return evaluate(stack.tag, nbtCondition);
    }
}
