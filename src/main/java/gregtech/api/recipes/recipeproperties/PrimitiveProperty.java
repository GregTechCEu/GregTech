package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Simple Marker Property to tell JEI to not display Total EU and EU/t.
 */
public class PrimitiveProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "primitive_property";
    private static PrimitiveProperty INSTANCE;

    private PrimitiveProperty() {
        super(KEY, Boolean.class);
    }

    public static PrimitiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PrimitiveProperty();
        }
        return INSTANCE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {}

    @Override
    public int getInfoHeight(Object value) {
        return 0;
    }

    @Override
    public boolean hideTotalEU() {
        return true;
    }

    @Override
    public boolean hideEUt() {
        return true;
    }
}
