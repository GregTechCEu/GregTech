package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class ImplosionExplosiveProperty extends RecipeProperty<ItemStack> {

    public static final String KEY = "explosives";

    private static ImplosionExplosiveProperty INSTANCE;


    private ImplosionExplosiveProperty() {
        super(KEY, ItemStack.class);
    }

    public static ImplosionExplosiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImplosionExplosiveProperty();
        }

        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.explosive",
                ((ItemStack) value).getDisplayName()), x, y, color);
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
