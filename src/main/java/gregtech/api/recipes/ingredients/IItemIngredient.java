package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.match.Matcher;

import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface IItemIngredient extends Matcher<ItemStack> {

    /**
     * Should be independent of the ItemStack's count.
     * @param stack the stack to match.
     * @return whether the stack satisfies this ingredient.
     */
    @Override
    boolean matches(ItemStack stack);

    /**
     * Return a list of all matching stacks within the given context. Try to operate on the lowest context possible
     * to avoid returning massive arrays. Note that custom NBT conditions should <i>not</i>
     * operate on NBT contexts, but rather one step below. Then the custom NBT condition is evaluated for incoming
     * stacks that match on that level. Operating on NBT contexts is instead for exact, .equals() NBT matching.
     * @param context the matching context.
     * @return matching stacks within the matching context.
     */
    @Nullable Collection<ItemStack> getMatchingStacksWithinContext(@NotNull ItemStackMatchingContext context);

    /**
     * @return the NBT matching predicate for this ingredient.
     */
    @Nullable NBTMatcher getMatcher();
}
