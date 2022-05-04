package gregtech.api.recipes;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.api.util.IngredientHashStrategy;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Arrays;

public class CountableIngredient {

    private final Ingredient ingredient;
    private final int count;
    private boolean nonConsumable = false;
    private boolean hasNBTMatchingCondition = false;
    private NBTcondition NBTMatchingCondition;

    public CountableIngredient(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        if (count <= 0) {
            this.count = 1;
            setNonConsumable();
        } else {
            this.count = count;
        }
    }

    public CountableIngredient(CountableIngredient countableIngredient, int count) {
        this.ingredient = countableIngredient.ingredient;
        this.count = count;
        this.nonConsumable = countableIngredient.nonConsumable;
        this.hasNBTMatchingCondition = countableIngredient.hasNBTMatchingCondition;
        this.NBTMatchingCondition = countableIngredient.NBTMatchingCondition;
    }

    public static CountableIngredient from(ItemStack stack) {
        return new CountableIngredient(Ingredient.fromStacks(stack), stack.getCount());
    }

    public static CountableIngredient from(ItemStack stack, int amount) {
        return new CountableIngredient(Ingredient.fromStacks(stack), amount);
    }

    public static CountableIngredient from(String oredict) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(oredict).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + oredict + ":", new IllegalArgumentException());
        return new CountableIngredient(new OreIngredient(oredict), 1);
    }

    public static CountableIngredient from(String oredict, int count) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(oredict).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + oredict + ":", new IllegalArgumentException());
        return new CountableIngredient(new OreIngredient(oredict), count);
    }

    public static CountableIngredient from(OrePrefix prefix, Material material) {
        return from(prefix, material, 1);
    }

    public static CountableIngredient from(OrePrefix prefix, Material material, int count) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(new UnificationEntry(prefix, material).toString()).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + new UnificationEntry(prefix, material) + ":", new IllegalArgumentException());
        return new CountableIngredient(new OreIngredient(new UnificationEntry(prefix, material).toString()), count);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    public boolean isNonConsumable() {
        return nonConsumable;
    }

    public CountableIngredient setNonConsumable() {
        this.nonConsumable = true;
        return this;
    }

    public boolean hasNBTMatchingCondition() {
        return hasNBTMatchingCondition;
    }

    public NBTcondition getNBTMatchingCondition() {
        return NBTMatchingCondition;
    }

    public CountableIngredient setNBTMatchingCondition(NBTcondition condition) {
        this.hasNBTMatchingCondition = true;
        this.NBTMatchingCondition = condition;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountableIngredient that = (CountableIngredient) o;
        return count == that.count && IngredientHashStrategy.INSTANCE.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return IngredientHashStrategy.INSTANCE.hashCode(ingredient) + 31 * count;
    }

    @Override
    public String toString() {
        return "CountableIngredient{" + "ingredient=" + Arrays.toString(ingredient.getMatchingStacks()) + ", count=" + count + '}';
    }

    /**
     * a class to build a NBT condition, matching a string, then checking if the NBT matches the condition, then returning if the NBT matches
     */
    public static class NBTcondition {
        private final String nbtString;
        private final CountableIngredient.Comparator comparator;
        private final Long targetValue;

        public NBTcondition(String nbtString, String comparator, Long targetValue) {
            this.nbtString = nbtString;
            this.comparator = CountableIngredient.Comparator.valueOf(comparator);
            this.targetValue = targetValue;
        }

        public boolean evaluate(ItemStack stack) {
            if (stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null) {
                    if (nbt.hasKey(nbtString)) {
                        Long value = nbt.getLong(nbtString);
                        switch (comparator) {
                            case EQUALS:
                                return value.equals(targetValue);
                            case NOT_EQUALS:
                                return !value.equals(targetValue);
                            case LESS_THAN:
                                return value < targetValue;
                            case GREATER_THAN:
                                return value > targetValue;
                            case LESS_THAN_OR_EQUALS:
                                return value <= targetValue;
                            case GREATER_THAN_OR_EQUALS:
                                return value >= targetValue;
                        }
                    }
                }
            }
            return false;
        }
    }

    enum Comparator {

        LESS_THAN("<"), GREATER_THAN(">"), EQUALS("=="), NOT_EQUALS("!="), LESS_THAN_OR_EQUALS("<="), GREATER_THAN_OR_EQUALS(">=");

        public final String string;

        Comparator(String comparator) {
            this.string = comparator;
        }
    }

}
