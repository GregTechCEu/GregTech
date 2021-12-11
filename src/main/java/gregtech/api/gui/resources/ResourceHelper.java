package gregtech.api.gui.resources;

import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * copy from com.brandon3055.draconicevolution.helpers;
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
            InputStream inputstream = ResourceHelper.class.getResourceAsStream(String.format("/assets/%s/%s", GTValues.MODID, rs));
            if(inputstream == null) {
                return false;
            }
            IOUtils.closeQuietly(inputstream);
            cachedResources.put(rs, new ResourceLocation(GTValues.MODID, rs));
        }
        return true;
    }

    public static boolean isTextureExist(ResourceLocation textureResource) {
        InputStream inputstream = ResourceHelper.class.getResourceAsStream(String.format("/assets/%s/textures/%s.png", textureResource.getNamespace(), textureResource.getPath()));
        if(inputstream == null) {
            return false;
        }
        IOUtils.closeQuietly(inputstream);
        return true;
    }

}
