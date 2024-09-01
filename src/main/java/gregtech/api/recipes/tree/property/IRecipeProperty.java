package gregtech.api.recipes.tree.property;

import org.jetbrains.annotations.Nullable;

public interface IRecipeProperty {

    /**
     * {@link IRecipeProperty}s should be limited in scope using this method.
     */
    boolean propertyEquals(@Nullable IRecipeProperty other);

    /**
     * {@link IRecipeProperty}s should strive to avoid hash collisions with <i>all other instances</i> of
     * {@link IRecipeProperty} using this method.
     */
    int propertyHash();
    // reserved hashes:
    // -128 <> 127 circuit properties
    // 128 voltage property
}
