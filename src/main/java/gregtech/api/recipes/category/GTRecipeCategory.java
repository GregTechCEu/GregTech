package gregtech.api.recipes.category;

import gregtech.api.recipes.RecipeMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
    @NotNull
    public static GTRecipeCategory create(@NotNull String modid, @NotNull String categoryName,
                                          @NotNull String translationKey, @NotNull RecipeMap<?> recipeMap) {
        return categories.computeIfAbsent(categoryName,
                (k) -> new GTRecipeCategory(modid, categoryName, translationKey, recipeMap));
    }

    /**
     * @param categoryName the name of the category
     * @return the category associated with the name
     */
    @Nullable
    public static GTRecipeCategory getByName(@NotNull String categoryName) {
        return categories.get(categoryName);
    }

    private GTRecipeCategory(@NotNull String modid, @NotNull String name, @NotNull String translationKey,
                             @NotNull RecipeMap<?> recipeMap) {
        this.modid = modid;
        this.name = name;
        this.uniqueID = modid + ':' + this.name;
        this.translationKey = translationKey;
        this.recipeMap = recipeMap;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public String getModid() {
        return this.modid;
    }

    @NotNull
    public String getUniqueID() {
        return this.uniqueID;
    }

    @NotNull
    public String getTranslationKey() {
        return this.translationKey;
    }

    @NotNull
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

    @NotNull
    @Override
    public String toString() {
        return "GTRecipeCategory{" + uniqueID + '}';
    }
}
