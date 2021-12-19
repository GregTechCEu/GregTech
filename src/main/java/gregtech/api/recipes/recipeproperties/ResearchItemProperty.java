package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class ResearchItemProperty extends RecipeProperty<ItemStack>{
    public static final String KEY = "research_item";

    private static ResearchItemProperty INSTANCE;

    protected ResearchItemProperty() {
        super(KEY, ItemStack.class);
    }

    public static ResearchItemProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchItemProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {

    }
}
