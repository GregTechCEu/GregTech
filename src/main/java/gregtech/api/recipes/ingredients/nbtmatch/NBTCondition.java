package gregtech.api.recipes.ingredients.nbtmatch;

import gregtech.api.util.GTLog;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class is used to check if a NBT tag matches a condition, not necessarily matching the original item tag
 */
public class NBTCondition {

    public static final NBTCondition ANY = new NBTCondition(); // Special-case

    public static NBTCondition create(NBTTagType tagType, String nbtKey, Object value) {
        if (tagType == NBTTagType.LIST) {
            throw new IllegalArgumentException("Use ListNBTCondition::create instead of NBTCondition::create");
        }
        return new NBTCondition(tagType, nbtKey, value);
    }

    @Nullable
    public final NBTTagType tagType;
    public final String nbtKey;
    public final Object value;

    private NBTCondition() {
        this.tagType = null;
        this.nbtKey = null;
        this.value = null;
    }

    @SuppressWarnings("NullableProblems")
    protected NBTCondition(NBTTagType tagType, String nbtKey, Object value) {
        this.tagType = tagType;
        this.nbtKey = nbtKey;
        this.value = value;
        if (tagType == null || nbtKey == null || value == null) {
            GTLog.logger.error("NBTCondition must not have null parameters.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
        }
    }

    @Override
    public String toString() {
        return nbtKey + ": " + value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagType, nbtKey, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NBTCondition) {
            NBTCondition o = (NBTCondition) obj;
            return this.tagType == o.tagType && this.nbtKey.equals(o.nbtKey) && this.value.equals(o.value);
        }
        return false;
    }
}
