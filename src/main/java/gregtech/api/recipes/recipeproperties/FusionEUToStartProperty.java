package gregtech.api.recipes.recipeproperties;

import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.TreeMap;

public class FusionEUToStartProperty extends RecipeProperty<Long> {

    public static final String KEY = "eu_to_start";

    private static final TreeMap<Long, String> registeredFusionTiers = new TreeMap<>();

    private static FusionEUToStartProperty INSTANCE;

    protected FusionEUToStartProperty() {
        super(KEY, Long.class);
    }

    public static FusionEUToStartProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FusionEUToStartProperty();
        }

        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.eu_to_start",
                TextFormattingUtil.formatLongToCompactString(castValue(value))) + getFusionTier(castValue(value)), x, y, color);
    }

    private String getFusionTier(Long eu) {

        Map.Entry<Long, String> mapEntry = registeredFusionTiers.ceilingEntry(eu);

        if (mapEntry == null) {
            throw new IllegalArgumentException("Value is above registered maximum EU values");
        }

        return String.format(" %s", mapEntry.getValue());
    }

    public static void registerFusionTier(int tier, String shortName) {
        Validate.notNull(shortName);
        long maxEU = 16 * 10000000L * (long) Math.pow(2, tier - 6);
        registeredFusionTiers.put(maxEU, shortName);

    }
}
