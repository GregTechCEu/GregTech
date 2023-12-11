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

public class SimpleOrientedCubeRenderer implements ICubeRenderer {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private Map<CubeSide, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    private Map<CubeSide, TextureAtlasSprite> spritesEmissive;

    private enum CubeSide {

        FRONT,
        BACK,
        RIGHT,
        LEFT,
        TOP,
        BOTTOM;

        public static final CubeSide[] VALUES = values();
    }

    public SimpleOrientedCubeRenderer(String basePath) {
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
        this.sprites = new EnumMap<>(CubeSide.class);
        this.spritesEmissive = new EnumMap<>(CubeSide.class);
        for (CubeSide cubeSide : CubeSide.VALUES) {
            String fullPath = String.format("blocks/%s/%s", basePath, cubeSide.name().toLowerCase());
            this.sprites.put(cubeSide, textureMap.registerSprite(new ResourceLocation(modID, fullPath)));
            String emissive = fullPath + EMISSIVE;
            if (ResourceHelper.doResourcepacksHaveTexture(modID, emissive, true)) {
                this.spritesEmissive.put(cubeSide, textureMap.registerSprite(new ResourceLocation(modID, emissive)));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(CubeSide.FRONT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        IVertexOperation[] lightPipeline = ConfigHolder.client.machinesEmissiveTextures ?
                ArrayUtils.add(pipeline, new LightMapOperation(240, 240)) : pipeline;

        // Front
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT),
                BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.FRONT)) Textures.renderFace(renderState, translation, lightPipeline,
                frontFacing, bounds, sprites.get(CubeSide.FRONT), BloomEffectUtil.getEffectiveBloomLayer());

        // Back
        Textures.renderFace(renderState, translation, pipeline, frontFacing.getOpposite(), bounds,
                sprites.get(CubeSide.BACK), BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.BACK))
            Textures.renderFace(renderState, translation, lightPipeline, frontFacing.getOpposite(), bounds,
                    sprites.get(CubeSide.BACK), BloomEffectUtil.getEffectiveBloomLayer());

        // Left
        // best guess in this weird case
        EnumFacing left = frontFacing.getAxis() != EnumFacing.Axis.Y ? frontFacing.rotateYCCW() : EnumFacing.NORTH;
        Textures.renderFace(renderState, translation, pipeline, left, bounds,
                sprites.get(CubeSide.LEFT), BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.LEFT)) Textures.renderFace(renderState, translation, lightPipeline,
                left, bounds, sprites.get(CubeSide.LEFT), BloomEffectUtil.getEffectiveBloomLayer());

        // Right
        Textures.renderFace(renderState, translation, pipeline, left.getOpposite(), bounds,
                sprites.get(CubeSide.RIGHT), BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.RIGHT))
            Textures.renderFace(renderState, translation, lightPipeline, left.getOpposite(), bounds,
                    sprites.get(CubeSide.RIGHT), BloomEffectUtil.getEffectiveBloomLayer());

        // Up
        // best guess in this weird case
        EnumFacing up = frontFacing.getAxis() != EnumFacing.Axis.Y ? EnumFacing.UP : EnumFacing.WEST;
        Textures.renderFace(renderState, translation, pipeline, up, bounds, sprites.get(CubeSide.TOP),
                BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.TOP)) Textures.renderFace(renderState, translation, lightPipeline,
                up, bounds, sprites.get(CubeSide.TOP), BloomEffectUtil.getEffectiveBloomLayer());

        // Down
        Textures.renderFace(renderState, translation, pipeline, up.getOpposite(), bounds, sprites.get(CubeSide.BOTTOM),
                BlockRenderLayer.CUTOUT_MIPPED);
        if (spritesEmissive.containsKey(CubeSide.BOTTOM)) Textures.renderFace(renderState, translation, lightPipeline,
                up.getOpposite(), bounds, sprites.get(CubeSide.BOTTOM), BloomEffectUtil.getEffectiveBloomLayer());
    }
}
