package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SimpleSidedCubeRenderer implements ICubeRenderer {

    public enum RenderSide {

        TOP,
        BOTTOM,
        SIDE;

        public static final RenderSide[] VALUES = values();

        public static RenderSide bySide(EnumFacing side) {
            if (side == EnumFacing.UP) {
                return TOP;
            } else if (side == EnumFacing.DOWN) {
                return BOTTOM;
            } else return SIDE;
        }
    }

    protected final String basePath;

    @SideOnly(Side.CLIENT)
    protected Map<RenderSide, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    protected Map<RenderSide, TextureAtlasSprite> spritesEmissive;

    public SimpleSidedCubeRenderer(String basePath) {
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
        this.sprites = new EnumMap<>(RenderSide.class);
        this.spritesEmissive = new EnumMap<>(RenderSide.class);
        for (RenderSide overlayFace : RenderSide.VALUES) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(modID,
                    String.format("blocks/%s/%s", basePath, faceName));
            sprites.put(overlayFace, textureMap.registerSprite(resourceLocation));

            String emissive = String.format("blocks/%s/%s_emissive", basePath, faceName);
            if (ResourceHelper.doResourcepacksHaveTexture(modID, emissive, true)) {
                spritesEmissive.put(overlayFace, textureMap.registerSprite(new ResourceLocation(modID, emissive)));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSpriteOnSide(RenderSide renderSide) {
        return sprites.get(renderSide);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return getSpriteOnSide(RenderSide.TOP);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        RenderSide overlayFace = RenderSide.bySide(frontFacing);
        TextureAtlasSprite renderSprite = sprites.get(overlayFace);
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, renderSprite,
                BlockRenderLayer.CUTOUT_MIPPED);
        TextureAtlasSprite spriteEmissive = spritesEmissive.get(overlayFace);
        if (spriteEmissive != null) {
            if (ConfigHolder.client.machinesEmissiveTextures) {
                IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, spriteEmissive,
                        BloomEffectUtil.getEffectiveBloomLayer());
            } else Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, spriteEmissive,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }
}
