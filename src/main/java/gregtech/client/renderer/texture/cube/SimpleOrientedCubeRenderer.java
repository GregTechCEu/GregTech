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

public class SimpleOrientedCubeRenderer implements ICubeRenderer {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private Map<CubeSide, TextureAtlasSprite> sprites;

    @SideOnly(Side.CLIENT)
    private Map<CubeSide, TextureAtlasSprite> spritesEmissive;

    private enum CubeSide {
        FRONT, BACK, RIGHT, LEFT, TOP, BOTTOM
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
        for (CubeSide cubeSide : CubeSide.values()) {
            String fullPath = String.format("blocks/%s/%s", basePath, cubeSide.name().toLowerCase());
            this.sprites.put(cubeSide, textureMap.registerSprite(new ResourceLocation(modID, fullPath)));
            ResourceLocation emissiveLocation = new ResourceLocation(modID, fullPath + "_emissive");
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                this.spritesEmissive.put(cubeSide, textureMap.registerSprite(emissiveLocation));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(CubeSide.FRONT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        Textures.renderFace(renderState, translation, pipeline, EnumFacing.UP, bounds, sprites.get(CubeSide.TOP), BlockRenderLayer.CUTOUT_MIPPED);
        Textures.renderFace(renderState, translation, pipeline, EnumFacing.DOWN, bounds, sprites.get(CubeSide.BOTTOM), BlockRenderLayer.CUTOUT_MIPPED);

        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT), BlockRenderLayer.CUTOUT_MIPPED);
        Textures.renderFace(renderState, translation, pipeline, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK), BlockRenderLayer.CUTOUT_MIPPED);

        Textures.renderFace(renderState, translation, pipeline, frontFacing.rotateY(), bounds, sprites.get(CubeSide.LEFT), BlockRenderLayer.CUTOUT_MIPPED);
        Textures.renderFace(renderState, translation, pipeline, frontFacing.rotateYCCW(), bounds, sprites.get(CubeSide.RIGHT), BlockRenderLayer.CUTOUT_MIPPED);

        IVertexOperation[] lightPipeline = ConfigHolder.client.machinesEmissiveTextures ?
                ArrayUtils.add(pipeline, new LightMapOperation(240, 240)) : pipeline;

        if (spritesEmissive.containsKey(CubeSide.TOP)) Textures.renderFace(renderState, translation, lightPipeline, EnumFacing.UP, bounds, sprites.get(CubeSide.TOP), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.BOTTOM)) Textures.renderFace(renderState, translation, lightPipeline, EnumFacing.DOWN, bounds, sprites.get(CubeSide.BOTTOM), BloomEffectUtil.getRealBloomLayer());

        if (spritesEmissive.containsKey(CubeSide.FRONT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.BACK)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK), BloomEffectUtil.getRealBloomLayer());

        if (spritesEmissive.containsKey(CubeSide.LEFT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.rotateY(), bounds, sprites.get(CubeSide.LEFT), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.RIGHT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.rotateYCCW(), bounds, sprites.get(CubeSide.RIGHT), BloomEffectUtil.getRealBloomLayer());
    }
    
}
