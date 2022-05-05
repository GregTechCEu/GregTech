package gregtech.api.recipes.ingredients.NBTMatching;

/**
 * This class is used to check if a NBT tag matches a condition, not necessarily matching the original item tag
 */
public class NBTcondition {

    String nbtKey;
    long value;

    public static NBTcondition ANY;

    public NBTcondition(String nbtKey, long value) {
        this.nbtKey = nbtKey;
        this.value = value;
    }

}
