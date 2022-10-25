package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SidedCubeRenderer;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

public class FireboxActiveRenderer extends SidedCubeRenderer {

    public FireboxActiveRenderer(String basePath, OrientedOverlayRenderer.OverlayFace... faces) {
        super(basePath, faces);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            OrientedOverlayRenderer.OverlayFace overlayFace = OrientedOverlayRenderer.OverlayFace.bySide(facing, frontFacing);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            if (renderSprite != null) {
                if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                    Textures.renderFace(renderState, translation, ArrayUtils.add(pipeline, new ColourOperation(0xffffffff)), facing, bounds, renderSprite, BlockRenderLayer.CUTOUT_MIPPED);
                } else {
                    Textures.renderFace(renderState, translation, pipeline, facing, bounds, renderSprite, BlockRenderLayer.CUTOUT_MIPPED);
                }
                TextureAtlasSprite emissiveSprite = spritesEmissive.get(overlayFace);
                if (emissiveSprite != null && facing != frontFacing && facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                    if (ConfigHolder.client.machinesEmissiveTextures) {
                        Textures.renderFace(renderState, translation, ArrayUtils.add(pipeline, new ColourOperation(0xffffffff)), facing, bounds, emissiveSprite, BloomEffectUtil.getRealBloomLayer());
                    } else {
                        Textures.renderFace(renderState, translation, pipeline, facing, bounds, emissiveSprite, BlockRenderLayer.CUTOUT_MIPPED);
                    }
                }
            }
        }
    }
}
