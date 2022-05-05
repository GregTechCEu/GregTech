package gregtech.api.recipes.ingredients.NBTMatching;

import gregtech.api.recipes.CountableIngredient;

/**
 * a class to build a NBT condition, matching a string, then checking if the NBT matches the condition, then returning if the NBT matches
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
