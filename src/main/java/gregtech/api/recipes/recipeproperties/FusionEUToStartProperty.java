package gregtech.api.recipes.recipeproperties;

import gregtech.api.util.TextFormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.TreeMap;

public class FusionEUToStartProperty extends RecipeProperty<Long> {

    public static final String KEY = "eu_to_start";

    private static final TreeMap<Long, Pair<Integer, String>> registeredFusionTiers = new TreeMap<>();

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
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.eu_to_start",
                TextFormattingUtil.formatLongToCompactString(castValue(value))) + getFusionTierName(castValue(value)),
                x, y,
                color);
    }

    private static String getFusionTierName(Long eu) {
        Map.Entry<Long, Pair<Integer, String>> mapEntry = registeredFusionTiers.ceilingEntry(eu);

        if (mapEntry == null) {
            throw new IllegalArgumentException("Value is above registered maximum EU values");
        }

        return String.format(" %s", mapEntry.getValue().getRight());
    }

    public static int getFusionTier(Long eu) {
        Map.Entry<Long, Pair<Integer, String>> mapEntry = registeredFusionTiers.ceilingEntry(eu);
        return mapEntry == null ? 0 : mapEntry.getValue().getLeft();
    }

    public static void registerFusionTier(int tier, String shortName) {
        Validate.notNull(shortName);
        long maxEU = 16 * 10000000L * (long) Math.pow(2, tier - 6);
        registeredFusionTiers.put(maxEU, Pair.of(tier, shortName));
    }
}
