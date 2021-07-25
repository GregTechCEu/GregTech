package gregtech.integration.jei.utils.render;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.HashMap;

/**
 * HashMap of IDrawables for JEI rendering
 */
public class DrawableRegistry {
    private static Map<String, IDrawable> drawableMap = new HashMap<>();

    public static void initDrawable(IGuiHelper guiHelper, String textureLocation, int width, int height, String key) {
        drawableMap.put(key, guiHelper.createDrawable(new ResourceLocation(textureLocation), 0, 0, width, height, width, height));
    }

    public static void drawDrawable(Minecraft minecraft, String key, int x, int y) {
        drawableMap.get(key).draw(minecraft, x, y);
    }
}
