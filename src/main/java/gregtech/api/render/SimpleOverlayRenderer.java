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

import javax.annotation.Nullable;

public class SimpleOverlayRenderer implements IOverlayRenderer, IIconRegister {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    @Nullable
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite spriteEmissive;

    public SimpleOverlayRenderer(String basePath) {
        this.basePath = basePath;
        Textures.CUBE_SIDE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprite = textureMap.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/" + basePath));
        ResourceLocation emissiveLocation = new ResourceLocation(GTValues.MODID, "blocks/" + basePath + "_emissive");
        if (ResourceHelper.isTextureExist(emissiveLocation)) {
            this.spriteEmissive = textureMap.registerSprite(emissiveLocation);
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderSided(EnumFacing side, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline, Matrix4 translation) {
        Textures.renderFace(renderState, translation, pipeline, side, bounds, sprite);
        if (spriteEmissive != null) {
            if (ConfigHolder.client.machinesEmissiveTextures) {
                IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                Textures.renderFaceBloom(renderState, translation, lightPipeline, side, bounds, spriteEmissive);
            } else Textures.renderFace(renderState, translation, pipeline, side, bounds, spriteEmissive);
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderSided(EnumFacing side, CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderSided(side, Cuboid6.full, renderState, pipeline, translation);
    }
}
