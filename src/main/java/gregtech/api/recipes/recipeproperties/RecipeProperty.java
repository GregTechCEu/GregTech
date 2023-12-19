package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Objects;

public abstract class RecipeProperty<T> {

    private final Class<T> type;
    private final String key;

    protected RecipeProperty(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    @SideOnly(Side.CLIENT)
    public abstract void drawInfo(Minecraft minecraft, int x, int y, int color, Object value);

    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value, int mouseX, int mouseY) {
        drawInfo(minecraft, x, y, color, value);
    }

    @SideOnly(Side.CLIENT)
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {}

    public int getInfoHeight(Object value) {
        return 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    public boolean isOfType(Class<?> otherType) {
        return this.type == otherType;
    }

    public String getKey() {
        return key;
    }

    public T castValue(Object value) {
        return this.type.cast(value);
    }

    /**
     * Controls if the property should display any information in JEI
     *
     * @return true to hide information from JEI
     */
    public boolean isHidden() {
        return false;
    }

    /**
     * Whether to hide the Total EU tooltip for the recipe in JEI.
     */
    public boolean hideTotalEU() {
        return false;
    }

    /**
     * Whether to hide the EU/t tooltip for the recipe in JEI.
     */
    public boolean hideEUt() {
        return false;
    }

    /**
     * Whether to hide the Duration tooltip for the recipe in JEI.
     */
    public boolean hideDuration() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeProperty<?> that = (RecipeProperty<?>) o;
        return Objects.equals(type, that.type) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key);
    }
}
