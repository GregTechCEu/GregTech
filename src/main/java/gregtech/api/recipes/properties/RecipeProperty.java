package gregtech.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class RecipeProperty<T> {

    private final Class<T> type;
    private final String key;

    protected RecipeProperty(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    /**
     * @param value the value to serialize
     * @return the serialized form of the value
     */
    public abstract @NotNull NBTBase serialize(@NotNull Object value);

    /**
     * @param nbt the nbt to deserialize
     * @return the deserialized property value
     */
    public abstract @NotNull Object deserialize(@NotNull NBTBase nbt);

    @SideOnly(Side.CLIENT)
    public abstract void drawInfo(Minecraft minecraft, int x, int y, int color, Object value);

    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value, int mouseX, int mouseY) {
        drawInfo(minecraft, x, y, color, value);
    }

    @SideOnly(Side.CLIENT)
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {}

    public int getInfoHeight(@NotNull Object value) {
        return 10; // GTRecipeWrapper#LINE_HEIGHT
    }

    public final @NotNull String getKey() {
        return key;
    }

    protected final T castValue(@NotNull Object value) {
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeProperty<?>that)) return false;

        return type.equals(that.type) && getKey().equals(that.getKey());
    }

    @Override
    public final int hashCode() {
        return 31 * type.hashCode() + getKey().hashCode();
    }

    @Override
    public String toString() {
        return "RecipeProperty{" + "key='" + key + "'}";
    }
}
