package gregtech.api.recipes.ingredients;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class CraftTweakerItemInputWrapper extends GTRecipeInput {

    private final IIngredient ingredient;

    public CraftTweakerItemInputWrapper(IIngredient ingredient) {
        this.ingredient = ingredient;
        this.amount = ingredient.getAmount();
    }

    public static CraftTweakerItemInputWrapper getOrCreate(IIngredient ingredient, int i) {
        return (CraftTweakerItemInputWrapper) getFromCache(new CraftTweakerItemInputWrapper(ingredient));
    }

    @Override
    protected CraftTweakerItemInputWrapper copy() {
        CraftTweakerItemInputWrapper copy = new CraftTweakerItemInputWrapper(this.ingredient.amount(this.amount));
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public CraftTweakerItemInputWrapper copyWithAmount(int amount) {
        CraftTweakerItemInputWrapper copy = new CraftTweakerItemInputWrapper(this.ingredient.amount(amount));
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public ItemStack[] getInputStacks() {
        ItemStack[] stacks = new ItemStack[this.ingredient.getItems().size()];
        List<IItemStack> items = this.ingredient.getItems();
        for (int i = 0; i < items.size(); i++) {
            IItemStack iitem = items.get(i);
            ItemStack stack = CraftTweakerMC.getItemStack(iitem);
            stacks[i] = stack;
        }

        return stacks;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        IItemStack[] itemArray = this.ingredient.getItemArray();
        if (itemArray.length == 0) return true;

        return ingredient.getItems().stream().anyMatch(ii -> ii.matches(CraftTweakerMC.getIItemStackForMatching(itemStack, ii.getMetadata() == GTValues.W)));
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof CraftTweakerItemInputWrapper)) return false;

        CraftTweakerItemInputWrapper other = (CraftTweakerItemInputWrapper) input;

        if (this.isConsumable != other.isConsumable) return false;

        if (this.ingredient.getItems().size() != other.ingredient.getItems().size()) return false;
        for (int i = 0; i < this.ingredient.getItems().size(); i++) {
            if (!this.ingredient.getItems().get(i).matches(other.ingredient.getItems().get(i))) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (ItemStack stack : getInputStacks()) {
            hash = 31 * hash + stack.getItem().hashCode();
            hash = 31 * hash + stack.getMetadata();
            if (stack.hasTagCompound() && this.nbtMatcher == null) {
                hash = 31 * hash + stack.getTagCompound().hashCode();
            }
        }
        hash = 31 * hash + this.amount;
        hash = 31 * hash + (this.isConsumable ? 1 : 0);
        hash = 31 * hash + (this.nbtMatcher != null ? this.nbtMatcher.hashCode() : 0);
        hash = 31 * hash + (this.nbtCondition != null ? this.nbtCondition.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CraftTweakerItemInputWrapper)) return false;

        CraftTweakerItemInputWrapper other = (CraftTweakerItemInputWrapper) obj;

        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;

        if (this.ingredient.getItems().size() != other.ingredient.getItems().size()) return false;
        for (int i = 0; i < this.ingredient.getItems().size(); i++) {
            if (!this.ingredient.getItems().get(i).matches(other.ingredient.getItems().get(i))) return false;
        }

        return true;
    }

    @Override
    public boolean hasNBTMatchingCondition() {
        return true;
    }
}
