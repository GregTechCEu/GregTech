package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Definition of ItemStacks, Ore dicts, of ingredients for
 * use on RecipeMaps Recipes go here.
 * <p>
 * Forge uses are nor Hashable neither implement equals for these cases,
 * as they use a list of ItemStacks internally.
 * <p>
 * The behavior of the ingredient is determined by the GTingredient used.
 */
public abstract class GTRecipeInput {
    /**
     * All items will initially match the with is NBT (OreDicts have a null tag?)
     * but this behavior can be changed by using a NBTMatcher and an appropriate NBTCondition.
     */

    public int amount;
    boolean isConsumable = true;
    NBTMatcher nbtMatcher;
    NBTcondition nbtCondition;

    public int getAmount() {
        return amount;
    }

    abstract GTRecipeInput copy();

    abstract GTRecipeInput getFromCache(GTRecipeInput recipeInput);

    public GTRecipeInput setNonConsumable() {
        GTRecipeInput copy = copy();
        copy.isConsumable = false;
        return getFromCache(copy);
    }

    public GTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTcondition nbtCondition) {
        GTRecipeInput copy = copy();
        copy.nbtMatcher = nbtMatcher;
        copy.nbtCondition = nbtCondition;
        return getFromCache(copy);
    }

    public boolean hasNBTMatchingCondition() {
        return nbtMatcher != null;
    }

    public NBTMatcher getNBTMatcher() {
        return nbtMatcher;
    }

    public NBTcondition getNBTMatchingCondition() {
        return nbtCondition;
    }

    public boolean isNonConsumable() {
        return !isConsumable;
    }

    public ItemStack[] getInputStacks() {
        return null;
    }

    public FluidStack getInputFluidStack() {
        return null;
    }

    public boolean isItem() {
        return false;
    }

    public boolean isFluid() {
        return false;
    }

    public boolean isOreDict() {
        return false;
    }

    public int getOreDict() {
        return -1;
    }

    public boolean acceptsStack(@Nullable ItemStack input) {
        return false;
    }

    public boolean acceptsFluid(@Nullable FluidStack input) {
        return false;
    }

    /**
     * @return true if the input matches another input, while ignoring its amount field and
     * non-consumable status.
     *
     * used for unique input matching in RecipeMap
     * @see gregtech.api.recipes.RecipeMap#uniqueIngredientsList(List) (GTRecipeInput)
     */
    public abstract boolean equalIgnoreAmount(GTRecipeInput input);
}
