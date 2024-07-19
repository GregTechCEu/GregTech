package gregtech.api.recipes.recipeproperties;

import gregtech.common.ConfigHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class FuelProperty extends RecipeProperty<String> {
    public static final String KEY = "fuel";
    private static FuelProperty INSTANCE;
    private FuelProperty() {super( KEY, String.class ); }

    public static FuelProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FuelProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        for(int i = 1; i <= getTranslationLineCount(castValue(value)); i++) {
            minecraft.fontRenderer.drawString(I18n.format(getTranslationKey(castValue(value), i), getExtraInfo(castValue(value)) ), x, y+10*(i-getTranslationLineCount(castValue(value))), color);
        }
    }

    public String getExtraInfo(String unlocalizedName) {
        switch (unlocalizedName) {
            case "steam_turbine":
                return "" + 1.0 / ConfigHolder.machines.multiblockSteamToEU;
            default:
                return "";
        }
    }

    private static String getTranslationKey(String unlocalizedName, int lineNum) {
        return "gregtech.jei." + unlocalizedName + ".info." + lineNum;
    }

    private static int getTranslationLineCount(String unlocalizedName) {
        int i = 0;
        while( !I18n.format(getTranslationKey(unlocalizedName, i+1)).equals(getTranslationKey(unlocalizedName, i+1)) ) { i++; }
        return i;
    }

    @Override
    public int getInfoHeight(Object value) {
        return 10 * getTranslationLineCount(castValue(value));
    }

    @Override
    public int getInfoLineCount(Object value) { return getTranslationLineCount(castValue(value)); }
}
