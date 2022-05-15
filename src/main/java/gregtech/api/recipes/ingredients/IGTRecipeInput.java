package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import gregtech.api.recipes.ingredients.NBTMatching.NBTcondition;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * Definition of ItemStacks, Ore dicts, of ingredients for
 * use on RecipeMaps Recipes go here.
 *
 * Forge uses are nor Hashable neither implement equals for these cases,
 * as they use a list of ItemStacks internally.
 *
 * The behavior of the ingredient is determined by the GTingredient used.
*/
public interface IGTRecipeInput {
    /**
     * All items will initially match the with is NBT (OreDicts have a null tag?)
     * but this behavior can be changed by using a NBTMatcher and an appropriate NBTCondition.
     */

    int getAmount();

    IGTRecipeInput setNonConsumable();

    IGTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTcondition nbtCondition);

    boolean hasNBTMatchingCondition();

    NBTMatcher getNBTMatcher();

    NBTcondition getNBTMatchingCondition();

    boolean isNonConsumable();
    ItemStack getInputStack();
    FluidStack getInputFluidStack();
    default boolean isItem(){
        return false;
    }

    default boolean isFluid(){
        return false;
    }
    boolean isOreDict();
    default int getOreDict(){
        return -1;
    }
    boolean acceptsStack(@Nullable ItemStack input);
    }
