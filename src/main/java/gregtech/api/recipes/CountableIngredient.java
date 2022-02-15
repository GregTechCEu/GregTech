package gregtech.api.recipes;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.IngredientHashStrategy;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Arrays;

public class CountableIngredient {

    private final Ingredient ingredient;
    private final int count;
    private boolean nonConsumable = false;

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
            GTLog.logger.error("Tried to access item with oredict " + new UnificationEntry(prefix, material).toString() + ":", new IllegalArgumentException());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountableIngredient that = (CountableIngredient) o;
        return count == that.count &&
                IngredientHashStrategy.INSTANCE.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return IngredientHashStrategy.INSTANCE.hashCode(ingredient) + 31 * count;
    }

    @Override
    public String toString() {
        return "CountableIngredient{" +
                "ingredient=" + Arrays.toString(ingredient.getMatchingStacks()) +
                ", count=" + count +
                '}';
    }
}
