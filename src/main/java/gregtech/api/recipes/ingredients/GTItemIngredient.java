package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.match.Matcher;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface GTItemIngredient extends Matcher<ItemStack> {

    /**
     * Should be independent of the ItemStack's count.
     * 
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
     * 
     * @param context the matching context.
     * @return matching stacks within the matching context.
     */
    @NotNull
    Collection<ItemStack> getMatchingStacksWithinContext(@NotNull ItemStackMatchingContext context);

    @NotNull
    default List<ItemStack> getAllMatchingStacks() {
        List<ItemStack> collection = new ObjectArrayList<>();
        for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
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
        for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
            for (ItemStack stack : getMatchingStacksWithinContext(context)) {
                if (stack == null || stack.getItem() == Items.AIR || stack.isEmpty()) return false;
            }
        }
        return true;
    }
}
