package gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.ResourceHelper;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SidedCubeRenderer implements ICubeRenderer {

    protected final String basePath;
    protected final OverlayFace[] faces;

    @SideOnly(Side.CLIENT)
    protected Map<OverlayFace, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    protected Map<OverlayFace, TextureAtlasSprite> spritesEmissive;

    public SidedCubeRenderer(String basePath, OverlayFace... faces) {
        this.basePath = basePath;
        this.faces = faces;
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
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(modID, String.format("blocks/%s/%s", basePath, faceName));
            sprites.put(overlayFace, textureMap.registerSprite(resourceLocation));
            ResourceLocation emissiveLocation = new ResourceLocation(modID, String.format("blocks/%s/%s_emissive", basePath, faceName));
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                spritesEmissive.put(overlayFace, textureMap.registerSprite(emissiveLocation));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(OverlayFace.TOP);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            OverlayFace overlayFace = OverlayFace.bySide(facing, frontFacing);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            if (renderSprite != null) {
                Textures.renderFace(renderState, translation, pipeline, facing, bounds, renderSprite, BlockRenderLayer.CUTOUT_MIPPED);

                TextureAtlasSprite emissiveSprite = spritesEmissive.get(overlayFace);
                if (emissiveSprite != null) {
                    if (ConfigHolder.client.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                        Textures.renderFace(renderState, translation, lightPipeline, facing, bounds, emissiveSprite, BloomEffectUtil.getRealBloomLayer());
                    } else {
                        Textures.renderFace(renderState, translation, pipeline, facing, bounds, emissiveSprite, BlockRenderLayer.CUTOUT_MIPPED);
                    }
                }
            }
        }
    }
}
