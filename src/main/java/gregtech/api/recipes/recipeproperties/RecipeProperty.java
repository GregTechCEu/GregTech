package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;

import java.util.Objects;

public abstract class RecipeProperty<T> {
    private final Class<T> type;
    private final String key;

    protected RecipeProperty(String key, Class<T> type) {
        this.key = key;
        this.type = type;
    }

    public abstract void drawInfo(Minecraft minecraft, int x, int y, int color, Object value);

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
}
