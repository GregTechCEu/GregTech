package gregtech.api.recipes.ingredients.NBTMatching;

/**
 * This class is used to check if a NBT tag matches a condition, not necessarily matching the original item tag
 */
public class NBTCondition {

    protected String nbtKey;
    protected long value;

    public static NBTCondition ANY = new NBTCondition("ANY", 0);

    public NBTCondition(String nbtKey, long value) {
        this.nbtKey = nbtKey;
        this.value = value;
    }

    @Override
    public String toString() {
        return nbtKey + ": " + value;
    }
}
