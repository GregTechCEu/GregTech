package gregtech.api.recipes.category;

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
    private Object icon;

    @Nonnull
    public static GTRecipeCategory create(@Nonnull String modid, @Nonnull String name) {
        return categories.computeIfAbsent(name, (k) -> new GTRecipeCategory(modid, name));
    }

    private GTRecipeCategory(@Nonnull String modid, @Nonnull String name) {
        this.modid = modid;
        this.name = name;
        this.uniqueID = modid + ':' + name;
        this.unlocalizedName = modid + ".recipe.category." + name;
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
        return unlocalizedName;
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
}
