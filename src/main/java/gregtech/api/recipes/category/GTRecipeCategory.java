package gregtech.api.recipes.category;

import gregtech.api.recipes.RecipeMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GTRecipeCategory {

    private static final Map<String, GTRecipeCategory> categories = new Object2ObjectOpenHashMap<>();

    private final String modid;
    private final String name;
    private final String uniqueID;
    private final String translationKey;
    private final RecipeMap<?> recipeMap;
    private Object icon;

    /**
     * Create a GTRecipeCategory
     *
     * @param modid          the mod id of the category
     * @param categoryName   the name of the category
     * @param translationKey the translation key of the category.
     * @param recipeMap      the recipemap that accepts this category
     * @return the new category
     */
    @Nonnull
    public static GTRecipeCategory create(@Nonnull String modid, @Nonnull String categoryName,
                                          @Nonnull String translationKey, @Nonnull RecipeMap<?> recipeMap) {
        return categories.computeIfAbsent(categoryName,
                (k) -> new GTRecipeCategory(modid, categoryName, translationKey, recipeMap));
    }

    /**
     * @param categoryName the name of the category
     * @return the category associated with the name
     */
    @Nullable
    public static GTRecipeCategory getByName(@Nonnull String categoryName) {
        return categories.get(categoryName);
    }

    private GTRecipeCategory(@Nonnull String modid, @Nonnull String name, @Nonnull String translationKey,
                             @Nonnull RecipeMap<?> recipeMap) {
        this.modid = modid;
        this.name = name;
        this.uniqueID = modid + ':' + this.name;
        this.translationKey = translationKey;
        this.recipeMap = recipeMap;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public String getModid() {
        return this.modid;
    }

    @Nonnull
    public String getUniqueID() {
        return this.uniqueID;
    }

    @Nonnull
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Nonnull
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    /**
     * The icon can be an {@link net.minecraft.item.ItemStack} or an {@link gregtech.api.gui.resources.TextureArea},
     * or any other format supported by JEI.
     *
     * @param icon the icon to use as a JEI category
     * @return this
     */
    public GTRecipeCategory jeiIcon(@Nullable Object icon) {
        this.icon = icon;
        return this;
    }

    @Nullable
    public Object getJEIIcon() {
        return this.icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GTRecipeCategory that = (GTRecipeCategory) o;

        return getUniqueID().equals(that.getUniqueID());
    }

    @Override
    public int hashCode() {
        return getUniqueID().hashCode();
    }

    @Nonnull
    @Override
    public String toString() {
        return "GTRecipeCategory{" + uniqueID + '}';
    }
}
