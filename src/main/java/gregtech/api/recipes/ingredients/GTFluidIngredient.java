package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.match.Matcher;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;

import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface GTFluidIngredient extends Matcher<FluidStack> {

    /**
     * Should be independent of the FluidStack's amount.
     * 
     * @param stack the stack to match.
     * @return whether the stack satisfies this ingredient.
     */
    @Override
    boolean matches(FluidStack stack);

    default boolean consumes() {
        return true;
    }

    /**
     * Return a list of all matching stacks within the given context. Try to operate on the lowest context possible
     * to avoid returning massive arrays. Note that custom NBT conditions should <i>not</i>
     * operate on NBT contexts, but rather one step below. Then the custom NBT condition is evaluated for incoming
     * stacks that match on that level. Operating on NBT contexts is instead for exact, .equals() NBT matching.
     * 
     * @param context the matching context.
     * @return matching stacks within the matching context.
     */
    @NotNull
    Collection<FluidStack> getMatchingStacksWithinContext(@NotNull FluidStackMatchingContext context);

    @NotNull
    default List<FluidStack> getAllMatchingStacks() {
        List<FluidStack> collection = new ObjectArrayList<>();
        for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
            collection.addAll(getMatchingStacksWithinContext(context));
        }
        return collection;
    }

    /**
     * @return the NBT matching predicate for this ingredient.
     */
    @Nullable
    NBTMatcher getMatcher();

    /**
     * should conduct an internal poll as to the validness of this ingredient;
     * e.g. checking that all input stacks exist, for example.
     * 
     * @return whether this ingredient is valid
     */
    default boolean isValid() {
        for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
            Collection<FluidStack> stacks = getMatchingStacksWithinContext(context);
            for (FluidStack stack : stacks) {
                if (stack == null || stack.getFluid() == null || stack.amount == 0) return false;
            }
        }
        return true;
    }
}
