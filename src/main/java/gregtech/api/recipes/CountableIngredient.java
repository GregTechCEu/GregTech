package gregtech.api.recipes;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTLog;
import gregtech.api.util.IngredientHashStrategy;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IngredientNBT;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CountableIngredient {

    private final Ingredient ingredient;
    private final int count;
    private boolean nonConsumable = false;
    private String oreDict = null;
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

    public static CountableIngredient from(ItemStack stack) {
        return new CountableIngredient(IngredientNBT.fromStacks(stack), stack.getCount());
    }

    public static CountableIngredient from(ItemStack stack, int amount) {
        return new CountableIngredient(IngredientNBT.fromStacks(stack), amount);
    }

    public static CountableIngredient from(String oreDict) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(oreDict).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + oreDict + ":", new IllegalArgumentException());
        CountableIngredient ci = new CountableIngredient(new OreIngredient(oreDict), 1);
        ci.setOreDict(oreDict);
        return ci;
    }

    public static CountableIngredient from(String oreDict, int count) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(oreDict).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + oreDict + ":", new IllegalArgumentException());
        CountableIngredient ci = new CountableIngredient(new OreIngredient(oreDict), count);
        ci.setOreDict(oreDict);
        return ci;
    }

    public static CountableIngredient from(OrePrefix prefix, Material material) {
        return from(prefix, material, 1);
    }

    public static CountableIngredient from(OrePrefix prefix, Material material, int count) {
        if (ConfigHolder.misc.debug && OreDictionary.getOres(new UnificationEntry(prefix, material).toString()).isEmpty())
            GTLog.logger.error("Tried to access item with oredict " + new UnificationEntry(prefix, material) + ":", new IllegalArgumentException());
        CountableIngredient ci = new CountableIngredient(new OreIngredient(new UnificationEntry(prefix, material).toString()), count);
        ci.setOreDict(new UnificationEntry(prefix, material).toString());
        return ci;
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

    private void setOreDict(String oreDict) {
        this.oreDict = oreDict;
    }

    public boolean isOreDict() {
        return oreDict != null;
    }

    public String getOreDict() {
        return oreDict;
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
        return "CountableIngredient{" + "ingredient=" + Arrays.stream(ingredient.getMatchingStacks()).map(ItemStack::getDisplayName).collect(Collectors.toList()) + ", count=" + count + '}';
    }

}
