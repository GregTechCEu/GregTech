package gregtech.api.recipes.recipeproperties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

public class MobOnTopProperty extends RecipeProperty<ResourceLocation> {
    public static final String KEY = "mob_on_top";
    private static MobOnTopProperty INSTANCE;

    public static MobOnTopProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new MobOnTopProperty();
        return INSTANCE;
    }

    private MobOnTopProperty() {
        super(KEY, ResourceLocation.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.accepted_mobs_on_top",
                getTranslationName(castValue(value))), x, y, color);
    }

    private String getTranslationName(ResourceLocation location) {
        return location.equals(EntityList.PLAYER) ? I18n.format("mob_extractor.player_name") : EntityList.getTranslationName(location);
    }
}
