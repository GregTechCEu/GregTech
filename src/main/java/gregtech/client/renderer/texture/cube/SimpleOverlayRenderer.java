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
import org.jetbrains.annotations.Nullable;

public class SimpleOverlayRenderer implements ICubeRenderer {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sprite;

    @Nullable
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite spriteEmissive;

    public SimpleOverlayRenderer(String basePath) {
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
        this.sprite = textureMap.registerSprite(new ResourceLocation(modID, "blocks/" + basePath));
        String emissive = "blocks/" + basePath + EMISSIVE;
        if (ResourceHelper.doResourcepacksHaveTexture(modID, emissive, true)) {
            this.spriteEmissive = textureMap.registerSprite(new ResourceLocation(modID, emissive));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprite,
                BlockRenderLayer.CUTOUT_MIPPED);
        if (spriteEmissive != null) {
            if (ConfigHolder.client.machinesEmissiveTextures) {
                IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, spriteEmissive,
                        BloomEffectUtil.getEffectiveBloomLayer());
            } else Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, spriteEmissive,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprite;
    }
}
