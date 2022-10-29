package gregtech.api.recipes.ingredients;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Objects;

public class GTRecipeOreInput extends GTRecipeInput {
    int ore;
    ItemStack[] inputStacks;

    protected GTRecipeOreInput(String ore, int amount) {
        this.ore = OreDictionary.getOreID(ore);
        this.amount = amount;
    }

    protected GTRecipeOreInput(int ore, int amount) {
        this.ore = ore;
        this.amount = amount;
    }

    public static GTRecipeInput getOrCreate(String ore, int amount) {
        return getFromCache(new GTRecipeOreInput(ore, amount));
    }

    public static GTRecipeInput getOrCreate(String ore) {
        return getFromCache(new GTRecipeOreInput(ore, 1));
    }

    public static GTRecipeInput getOrCreate(OrePrefix prefix, Material material, int amount) {
        return getOrCreate(new UnificationEntry(prefix, material).toString(), amount);
    }

    public static GTRecipeInput getOrCreate(OrePrefix prefix, Material material) {
        return getOrCreate(new UnificationEntry(prefix, material).toString(), 1);
    }

    @Override
    protected GTRecipeOreInput copy() {
        GTRecipeOreInput copy = new GTRecipeOreInput(ore, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        GTRecipeOreInput copy = new GTRecipeOreInput(ore, amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    //The items returned here are not updated after its first call, so they are not suitable for use while recipes are being processed and
    //the OreDicts being modified.
    @Override
    public ItemStack[] getInputStacks() {
        if (this.inputStacks == null) {
            inputStacks = (OreDictionary.getOres(OreDictionary.getOreName(ore)).stream().map(is -> {
                is = is.copy();
                is.setCount(this.amount);
                return is;
            })).toArray(ItemStack[]::new);
        }
        return inputStacks;
    }

    @Override
    public boolean isOreDict() {
        return true;
    }

    @Override
    public int getOreDict() {
        return ore;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (ItemStack target : getInputStacks()) {
            if (OreDictionary.itemMatches(target, input, false)) {
                return nbtMatcher == null || nbtMatcher.evaluate(input, nbtCondition);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, ore, isConsumable, nbtMatcher, nbtCondition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeOreInput)) {
            return false;
        }
        GTRecipeOreInput other = (GTRecipeOreInput) obj;
        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return ore == other.ore;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeOreInput)) {
            return false;
        }
        GTRecipeOreInput other = (GTRecipeOreInput) input;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;
        return ore == other.ore;
    }
}
