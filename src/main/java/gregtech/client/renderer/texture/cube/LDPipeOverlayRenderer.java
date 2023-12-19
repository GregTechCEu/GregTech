package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

/**
 * Intended for removal. Is only meant of LD pipes with only one regular and optionally an emissive texture per face.
 * <p>
 * Use {@link AlignedOrientedOverlayRenderer} for everything else.
 */
@ApiStatus.Experimental
public class LDPipeOverlayRenderer extends AlignedOrientedOverlayRenderer {

    public LDPipeOverlayRenderer(@NotNull String basePath) {
        super(basePath);
    }

    /**
     * Copy-paste of {@link OrientedOverlayRenderer#registerIcons(TextureMap)} to get around missing texture logging.
     * <p>
     * Should remove this once a more robust texture system is put in place
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new EnumMap<>(OverlayFace.class);
        String modID = GTValues.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }

        boolean foundTexture = false;
        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            final String faceName = overlayFace.name().toLowerCase();
            final String overlayPath = String.format("blocks/%s/overlay_%s", basePath, faceName);

            // if a normal texture location is found, try to find the rest
            TextureAtlasSprite normalSprite = ICubeRenderer.getResource(textureMap, modID, overlayPath);
            // require the normal texture to get the rest
            if (normalSprite == null) continue;

            foundTexture = true;

            // emissive

            TextureAtlasSprite normalSpriteEmissive = ICubeRenderer.getResource(textureMap, modID,
                    overlayPath + EMISSIVE);

            sprites.put(overlayFace, new ActivePredicate(normalSprite, normalSprite, null,
                    normalSpriteEmissive, null, null));
        }

        if (!foundTexture) {
            FMLClientHandler.instance()
                    .trackMissingTexture(new ResourceLocation(modID, "blocks/" + basePath + "/overlay_OVERLAY_FACE"));
        }
    }
}
