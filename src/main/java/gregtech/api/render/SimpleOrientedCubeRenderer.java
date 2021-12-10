package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SimpleOrientedCubeRenderer implements IIconRegister {

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
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new EnumMap<>(CubeSide.class);
        this.spritesEmissive = new EnumMap<>(CubeSide.class);
        for (CubeSide cubeSide : CubeSide.values()) {
            String fullPath = String.format("blocks/%s/%s", basePath, cubeSide.name().toLowerCase());
            this.sprites.put(cubeSide, textureMap.registerSprite(new ResourceLocation(GTValues.MODID, fullPath)));
            ResourceLocation emissiveLocation = new ResourceLocation(GTValues.MODID, fullPath + "_emissive");
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                this.spritesEmissive.put(cubeSide, textureMap.registerSprite(emissiveLocation));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(CubeSide.FRONT);
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 bounds, EnumFacing frontFacing) {
        Textures.renderFace(renderState, translation, ops, EnumFacing.UP, bounds, sprites.get(CubeSide.TOP));
        Textures.renderFace(renderState, translation, ops, EnumFacing.DOWN, bounds, sprites.get(CubeSide.BOTTOM));

        Textures.renderFace(renderState, translation, ops, frontFacing, bounds, sprites.get(CubeSide.FRONT));
        Textures.renderFace(renderState, translation, ops, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK));

        Textures.renderFace(renderState, translation, ops, frontFacing.rotateY(), bounds, sprites.get(CubeSide.LEFT));
        Textures.renderFace(renderState, translation, ops, frontFacing.rotateYCCW(), bounds, sprites.get(CubeSide.RIGHT));

        IVertexOperation[] lightPipeline = ConfigHolder.client.machinesEmissiveTextures ?
                ArrayUtils.add(ops, new LightMapOperation(240, 240)) : ops;

        if (spritesEmissive.containsKey(CubeSide.TOP)) Textures.renderFaceBloom(renderState, translation, lightPipeline, EnumFacing.UP, bounds, sprites.get(CubeSide.TOP));
        if (spritesEmissive.containsKey(CubeSide.BOTTOM)) Textures.renderFaceBloom(renderState, translation, lightPipeline, EnumFacing.DOWN, bounds, sprites.get(CubeSide.BOTTOM));

        if (spritesEmissive.containsKey(CubeSide.FRONT)) Textures.renderFaceBloom(renderState, translation, lightPipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT));
        if (spritesEmissive.containsKey(CubeSide.BACK)) Textures.renderFaceBloom(renderState, translation, lightPipeline, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK));

        if (spritesEmissive.containsKey(CubeSide.LEFT)) Textures.renderFaceBloom(renderState, translation, lightPipeline, frontFacing.rotateY(), bounds, sprites.get(CubeSide.LEFT));
        if (spritesEmissive.containsKey(CubeSide.RIGHT)) Textures.renderFaceBloom(renderState, translation, lightPipeline, frontFacing.rotateYCCW(), bounds, sprites.get(CubeSide.RIGHT));
    }
}
