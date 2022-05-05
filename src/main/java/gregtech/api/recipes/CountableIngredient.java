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
    private NBTMatcher NBTMatcher;

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
        this.NBTMatcher = countableIngredient.NBTMatcher;
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

    public NBTMatcher getNBTMatcher() {
        return NBTMatcher;
    }

    public CountableIngredient setNBTMatchingCondition(NBTcondition condition, NBTMatcher matcher) {
        this.hasNBTMatchingCondition = true;
        this.NBTMatchingCondition = condition;
        this.NBTMatcher = matcher;
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

        String nbtKey;
        long value;

        public static NBTcondition ANY;

        public NBTcondition(String nbtKey, long value) {
            this.nbtKey = nbtKey;
            this.value = value;
        }

    }

    public static class NBTMatcher {

        public NBTMatcher(){

        }

        public static NBTMatcher ANY = new NBTMatcher() {
            @Override
            public boolean evaluate(ItemStack stack, NBTcondition NBTcondition) {
                return true;
            }
        };

        public static NBTMatcher LESS_THAN = new NBTMatcher() {
            @Override
            public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
                return getKeyValue(stack, nbtKey) < value;
            }
        };

        public static NBTMatcher LESS_THAN_OR_EQUALS = new NBTMatcher() {
            @Override
            public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
                return getKeyValue(stack, nbtKey) <= value;
            }
        };

        public static NBTMatcher EQUALS = new NBTMatcher() {
            @Override
            public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
                return getKeyValue(stack, nbtKey) == value;
            }
        };

        public static NBTMatcher GREATER_THAN_OR_EQUALS = new NBTMatcher() {
            @Override
            public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
                return getKeyValue(stack, nbtKey) >= value;
            }
        };

        public static NBTMatcher GREATER_THAN = new NBTMatcher() {
            @Override
            public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
                return getKeyValue(stack, nbtKey) > value;
            }
        };

        public boolean hasKey(ItemStack stack, String nbtKey) {
            if (stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null) {
                    return nbt.hasKey(nbtKey);
                }
            }
            return false;
        }

        public long getKeyValue(ItemStack stack, String nbtKey) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null) {
                return nbt.getLong(nbtKey);
            }
            return 0;
        }

        public boolean evaluate(ItemStack stack, NBTcondition NBTcondition) {
            return hasKey(stack, NBTcondition.nbtKey) && keyValueMatches(stack, NBTcondition.nbtKey, NBTcondition.value);
        }

        public boolean keyValueMatches(ItemStack stack, String nbtKey, Long value) {
            return false;
        }
    }
}
