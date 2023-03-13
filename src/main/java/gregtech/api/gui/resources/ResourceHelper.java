package gregtech.api.gui.resources;

import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Original copied from com.brandon3055.draconicevolution.helpers;
 * <p>
 * Modified for improved performance.
 */
public final class ResourceHelper {

    public static final String RESOURCE_PREFIX = GTValues.MODID + ":";

    private static final Map<String, ResourceLocation> cachedResources = new HashMap<>();
    private static final String DIR_FORMAT = "textures/%s.png";

    private ResourceHelper() {/**/}

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
    }

    public static ResourceLocation getResource(String rs) {
        if (!cachedResources.containsKey(rs)) {
            cachedResources.put(rs, new ResourceLocation(RESOURCE_PREFIX + rs));
        }
        return cachedResources.get(rs);
    }

    @SuppressWarnings("unused")
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

    /**
     * @param modid           the modid of the texture
     * @param textureResource the location of the texture
     * @param format          if the location should be formatted to include "textures/" and ".png"
     * @return if the resource exists
     */
    @SideOnly(Side.CLIENT)
    public static boolean doResourcepacksHaveTexture(@Nonnull String modid, @Nonnull String textureResource, boolean format) {
        if (format) textureResource = String.format(DIR_FORMAT, textureResource);
        return doResourcepacksHaveResource(modid, textureResource);
    }

    /**
     * @param modid           the modid of the texture, formatted with the root dir and file extension
     * @param resource the location of the resource
     * @return if the resource exists
     */
    @SideOnly(Side.CLIENT)
    public static boolean doResourcepacksHaveResource(@Nonnull String modid, @Nonnull String resource) {
        return doResourcepacksHaveResource(new ResourceLocation(modid, resource));
    }

    /**
     * @param resource the location of the resource, formatted with the root dir and file extension
     * @return if the resource exists
     */
    @SideOnly(Side.CLIENT)
    public static boolean doResourcepacksHaveResource(@Nonnull ResourceLocation resource) {
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        try {
            // check if the texture file exists
            manager.getResource(resource);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Does not check resourcepacks, only the file loading in the mod's folder
     *
     * @param modid           the modid of the texture
     * @param textureResource the location of the texture
     * @return if the resource exists
     */
    public static boolean isTextureExist(@Nonnull String modid, @Nonnull String textureResource) {
        URL url = ResourceHelper.class.getResource(String.format("/assets/%s/textures/%s.png", modid, textureResource));
        return url != null;
    }

    /**
     * Does not check resourcepacks, only the file loading in the mod's folder
     *
     * @param textureResource the location of the texture
     * @return if the resource exists
     */
    @SuppressWarnings("unused")
    public static boolean isTextureExist(@Nonnull ResourceLocation textureResource) {
        return isTextureExist(textureResource.getNamespace(), textureResource.getPath());
    }
}
