package gregtech.api.recipes.lookup.property;

import org.jetbrains.annotations.Nullable;

public interface IRecipeSearchProperty {

    /**
     * {@link IRecipeSearchProperty}s should be limited in scope using this method.
     */
    boolean propertyEquals(@Nullable IRecipeSearchProperty other);

    /**
     * {@link IRecipeSearchProperty}s should strive to avoid hash collisions with <i>all other instances</i> of
     * {@link IRecipeSearchProperty} using this method.
     */
    int propertyHash();
    // reserved hashes:
    // -128 <> 127 circuit properties
    // 128 power supply property
    // 129 power capacity property
    // 130 cleanroom property
    // 131 eu to start property
    // 132 temperature property
    // 133 dimension property
    // 134 CWUt property
    // 135 biome property
}
