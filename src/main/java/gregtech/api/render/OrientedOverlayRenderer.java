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

public class OrientedOverlayRenderer implements IIconRegister {

    public enum OverlayFace {
        FRONT, BACK, TOP, BOTTOM, SIDE;

        public static OverlayFace bySide(EnumFacing side, EnumFacing frontFacing) {
            if (side == frontFacing) {
                return FRONT;
            } else if (side.getOpposite() == frontFacing) {
                return BACK;
            } else if (side == EnumFacing.UP) {
                return TOP;
            } else if (side == EnumFacing.DOWN) {
                return BOTTOM;
            } else return SIDE;
        }
    }

    private final String basePath;
    private final OverlayFace[] faces;

    @SideOnly(Side.CLIENT)
    public Map<OverlayFace, ActivePredicate> sprites;

    @SideOnly(Side.CLIENT)
    public static class ActivePredicate {

        private final TextureAtlasSprite normalSprite;
        private final TextureAtlasSprite activeSprite;

        private final TextureAtlasSprite normalSpriteEmissive;
        private final TextureAtlasSprite activeSpriteEmissive;

        public ActivePredicate(TextureAtlasSprite normalSprite,
                               TextureAtlasSprite activeSprite,
                               TextureAtlasSprite normalSpriteEmissive,
                               TextureAtlasSprite activeSpriteEmissive) {
            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.normalSpriteEmissive = normalSpriteEmissive;
            this.activeSpriteEmissive = activeSpriteEmissive;
        }

        public TextureAtlasSprite getSprite(boolean active) {
            return active ? activeSprite : normalSprite;
        }

        public TextureAtlasSprite getEmissiveSprite(boolean active) {
            return active ? activeSpriteEmissive : normalSpriteEmissive;
        }
    }

    public OrientedOverlayRenderer(String basePath, OverlayFace... faces) {
        this.basePath = basePath;
        this.faces = faces;
        Textures.iconRegisters.add(this);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new EnumMap<>(OverlayFace.class);
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();

            ResourceLocation normalLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s", basePath, faceName));
            TextureAtlasSprite normalSprite = textureMap.registerSprite(normalLocation);
            ResourceLocation activeLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s_active", basePath, faceName));
            TextureAtlasSprite activeSprite = textureMap.registerSprite(activeLocation);

            ResourceLocation normalLocationEmissive = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s_emissive", basePath, faceName));
            TextureAtlasSprite normalSpriteEmissive = ResourceHelper.isTextureExist(normalLocationEmissive) ? textureMap.registerSprite(normalLocationEmissive) : null;
            ResourceLocation activeLocationEmissive = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s_active_emissive", basePath, faceName));
            TextureAtlasSprite activeSpriteEmissive = ResourceHelper.isTextureExist(activeLocationEmissive) ? textureMap.registerSprite(activeLocationEmissive) : null;
            sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, normalSpriteEmissive, activeSpriteEmissive));
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            ActivePredicate predicate = sprites.get(OverlayFace.bySide(renderSide, frontFacing));
            if (predicate != null) {

                TextureAtlasSprite renderSprite = predicate.getSprite(isActive);
                Textures.renderFace(renderState, translation, ops, renderSide, bounds, renderSprite);

                TextureAtlasSprite emissiveSprite = predicate.getEmissiveSprite(isActive);
                if (emissiveSprite != null) {
                    if (ConfigHolder.U.clientConfig.machinesEemissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.add(ops, new LightMapOperation(240, 240));
                        Textures.renderFaceBloom(renderState, translation, lightPipeline, renderSide, bounds, emissiveSprite);
                    } else {
                        // have to still render both overlays or else textures will be broken
                        Textures.renderFace(renderState, translation, ops, renderSide, bounds, emissiveSprite);
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, boolean isActive) {
        render(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive);
    }
}
