package gregtech.api.recipes.ingredients.nbtmatch;

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

    public final NBTTagType tagType;
    public final String nbtKey;
    public final Object value;

    private NBTCondition() {
        this.tagType = null;
        this.nbtKey = null;
        this.value = null;
    }

    protected NBTCondition(NBTTagType tagType, String nbtKey, Object value) {
        this.tagType = tagType;
        this.nbtKey = nbtKey;
        this.value = value;
    }

    @Override
    public String toString() {
        return nbtKey + ": " + value;
    }
}
