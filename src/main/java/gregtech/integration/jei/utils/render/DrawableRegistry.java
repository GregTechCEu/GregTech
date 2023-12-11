package gregtech.integration.jei.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap of IDrawables for JEI rendering
 */
public class DrawableRegistry {

    private static final Map<String, IDrawable> drawableMap = new HashMap<>();

    public static void initDrawable(IGuiHelper guiHelper, String textureLocation, int width, int height, String key) {
        drawableMap.put(key, guiHelper.drawableBuilder(new ResourceLocation(textureLocation), 0, 0, width, height)
                .setTextureSize(width, height).build());
    }

    public static void drawDrawable(Minecraft minecraft, String key, int x, int y) {
        drawableMap.get(key).draw(minecraft, x, y);
    }
}
