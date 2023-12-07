package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer.OverlayFace;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SidedCubeRenderer implements ICubeRenderer {

    private static final String BASE_DIR = "blocks/%s/%s";

    protected final String basePath;

    @SideOnly(Side.CLIENT)
    protected Map<OverlayFace, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    protected Map<OverlayFace, TextureAtlasSprite> spritesEmissive;

    public SidedCubeRenderer(String basePath) {
        this.basePath = basePath;
        Textures.CUBE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        String modID = GTValues.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        this.sprites = new EnumMap<>(OverlayFace.class);
        this.spritesEmissive = new EnumMap<>(OverlayFace.class);

        boolean foundTexture = false;
        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            final String faceName = overlayFace.name().toLowerCase();
            final String overlayPath = String.format(BASE_DIR, basePath, faceName);

            TextureAtlasSprite normalSprite = ICubeRenderer.getResource(textureMap, modID, overlayPath);
            // require the normal texture to get the rest
            if (normalSprite == null) continue;

            foundTexture = true;

            sprites.put(overlayFace, normalSprite);

            spritesEmissive.put(overlayFace, ICubeRenderer.getResource(textureMap, modID, overlayPath + EMISSIVE));
        }

        if (!foundTexture) {
            FMLClientHandler.instance()
                    .trackMissingTexture(new ResourceLocation(modID, "blocks/" + basePath + "/OVERLAY_FACE"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(OverlayFace.TOP);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            OverlayFace overlayFace = OverlayFace.bySide(facing, frontFacing);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            if (renderSprite != null) {
                Textures.renderFace(renderState, translation, pipeline, facing, bounds, renderSprite,
                        BlockRenderLayer.CUTOUT_MIPPED);

                TextureAtlasSprite emissiveSprite = spritesEmissive.get(overlayFace);
                if (emissiveSprite != null) {
                    if (ConfigHolder.client.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                        Textures.renderFace(renderState, translation, lightPipeline, facing, bounds, emissiveSprite,
                                BloomEffectUtil.getEffectiveBloomLayer());
                    } else {
                        Textures.renderFace(renderState, translation, pipeline, facing, bounds, emissiveSprite,
                                BlockRenderLayer.CUTOUT_MIPPED);
                    }
                }
            }
        }
    }
}
