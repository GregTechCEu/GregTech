package gregtech.api.gui.resources;

import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Original copied from com.brandon3055.draconicevolution.helpers;
 * <p>
 * Modified for improved performance.
 */
public class ResourceHelper {

    private static final Map<String, ResourceLocation> cachedResources = new HashMap<>();
    public static final String RESOURCE_PREFIX = GTValues.MODID + ":";

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public static ResourceLocation getResource(String rs) {
        if (!cachedResources.containsKey(rs)) {
            cachedResources.put(rs, new ResourceLocation(RESOURCE_PREFIX + rs));
        }
        return cachedResources.get(rs);
    }

    public static ResourceLocation getResourceRAW(String rs) {
        if (!cachedResources.containsKey(rs)) {
            cachedResources.put(rs, new ResourceLocation(rs));
        }
        return cachedResources.get(rs);
    }

    public static void bindTexture(String rs) {
        bindTexture(getResource(rs));
    }

    public static boolean isResourceExist(String rs) {
        if (!cachedResources.containsKey(rs)) {
            URL url = ResourceHelper.class.getResource(String.format("/assets/%s/%s", GTValues.MODID, rs));
            if (url == null) return false;
            cachedResources.put(rs, new ResourceLocation(GTValues.MODID, rs));
        }
        return true;
    }

    public static boolean isTextureExist(@Nonnull String modid, @Nonnull String textureResource) {
        URL url = ResourceHelper.class.getResource(String.format("/assets/%s/textures/%s.png", modid, textureResource));
        return url != null;
    }

    public static boolean isTextureExist(@Nonnull ResourceLocation textureResource) {
        return isTextureExist(textureResource.getNamespace(), textureResource.getPath());
    }
}
