package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;

public abstract class IndexedRecipeLookup extends AbstractRecipeLookup {

    protected final ObjectArrayList<Recipe> builtRecipes = new ObjectArrayList<>();

    /**
     * Gets a recipe at an index. Does not update until rebuild is called.
     * 
     * @throws IndexOutOfBoundsException if {@code index} equals or exceeds {@link #getBuiltRecipes()}'s size.
     * @param index the index to get the recipe for.
     * @return the recipe at the index.
     */
    public @NotNull Recipe getRecipeByIndex(int index) {
        return getBuiltRecipes().get(index);
    }

    @Override
    public @NotNull @UnmodifiableView List<Recipe> getBuiltRecipes() {
        return builtRecipes;
    }

    @Override
    protected final void rebuild() {
        Collection<Recipe> collection = rebuildInternal();
        builtRecipes.clear();
        builtRecipes.addAll(collection);
        builtRecipes.trim();
    }

    /**
     * Called when this indexed lookup needs to be rebuilt.
     * 
     * @return what should be copied to built recipes after build.
     */
    protected abstract Collection<Recipe> rebuildInternal();
}
