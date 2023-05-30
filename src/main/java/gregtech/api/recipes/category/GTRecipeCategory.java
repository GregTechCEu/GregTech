package gregtech.api.recipes.category;

import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public final class GTRecipeCategory {

    private static final Map<String, GTRecipeCategory> categories = new Object2ObjectOpenHashMap<>();

    private final String modid;
    private final String name;
    private final String uniqueID;
    private final String unlocalizedName;
    private final RecipeMap<?> recipeMap;
    private Object icon;

    /**
     * Create a GTRecipeCategory
     *
     * @param modid the mod id of the category
     * @param name the name of the category, used in translation. Uses the recipeMap's translation key if null.
     * @param recipeMap the recipemap that accepts this category
     * @return the new category
     */
    @Nonnull
    public static GTRecipeCategory create(@Nonnull String modid, @Nullable String name, @Nonnull RecipeMap<?> recipeMap) {
        String key = name == null ? recipeMap.getUnlocalizedName() : name;
        return categories.computeIfAbsent(key, (k) -> new GTRecipeCategory(modid, name, recipeMap));
    }

    private GTRecipeCategory(@Nonnull String modid, @Nullable String name, @Nonnull RecipeMap<?> recipeMap) {
        this.modid = modid;
        this.name = name == null ? recipeMap.getUnlocalizedName() : name;
        this.uniqueID = modid + ':' + this.name;
        this.unlocalizedName = name == null ? recipeMap.getTranslationKey() : modid + ".recipe.category." + name;
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
    public String getUnlocalizedName() {
        return this.unlocalizedName;
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

    @Override
    public String toString() {
        return "GTRecipeCategory{" + uniqueID + '}';
    }
}
