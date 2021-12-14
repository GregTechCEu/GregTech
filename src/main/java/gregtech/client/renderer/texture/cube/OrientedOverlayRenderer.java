package gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
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

public class OrientedOverlayRenderer implements ICubeRenderer, IIconRegister {

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

    protected final String basePath;
    protected final OverlayFace[] faces;

    @SideOnly(Side.CLIENT)
    public Map<OverlayFace, ActivePredicate> sprites;

    @SideOnly(Side.CLIENT)
    public static class ActivePredicate {

        private final TextureAtlasSprite normalSprite;
        private final TextureAtlasSprite activeSprite;
        private final TextureAtlasSprite pausedSprite;

        private final TextureAtlasSprite normalSpriteEmissive;
        private final TextureAtlasSprite activeSpriteEmissive;
        private final TextureAtlasSprite pausedSpriteEmissive;

        public ActivePredicate(TextureAtlasSprite normalSprite,
                               TextureAtlasSprite activeSprite,
                               TextureAtlasSprite pausedSprite,
                               TextureAtlasSprite normalSpriteEmissive,
                               TextureAtlasSprite activeSpriteEmissive,
                               TextureAtlasSprite pausedSpriteEmissive) {

            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.normalSpriteEmissive = normalSpriteEmissive;
            this.activeSpriteEmissive = activeSpriteEmissive;
            this.pausedSpriteEmissive = pausedSpriteEmissive;
        }

        public TextureAtlasSprite getSprite(boolean active, boolean workingEnabled) {
            if (active) {
                if (workingEnabled) {
                    return activeSprite;
                } else if (pausedSprite != null) {
                    return pausedSprite;
                }
            }
            return normalSprite;
        }

        public TextureAtlasSprite getEmissiveSprite(boolean active, boolean workingEnabled) {
            if (active) {
                if (workingEnabled) {
                    return activeSpriteEmissive;
                } else if (pausedSpriteEmissive != null) {
                    return pausedSpriteEmissive;
                }
            }
            return normalSpriteEmissive;
        }
    }

    public OrientedOverlayRenderer(String basePath, OverlayFace... faces) {
        this.basePath = basePath;
        this.faces = faces;
        Textures.CUBE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new EnumMap<>(OverlayFace.class);
        String modID = GTValues.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation normalLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s", basePath, faceName));
            TextureAtlasSprite normalSprite = textureMap.registerSprite(normalLocation);
            ResourceLocation activeLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_active", basePath, faceName));
            TextureAtlasSprite activeSprite = textureMap.registerSprite(activeLocation);
            ResourceLocation pausedLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_paused", basePath, faceName));
            TextureAtlasSprite pausedSprite = ResourceHelper.isTextureExist(pausedLocation) ? textureMap.registerSprite(pausedLocation) : null;

            ResourceLocation normalLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_emissive", basePath, faceName));
            TextureAtlasSprite normalSpriteEmissive = ResourceHelper.isTextureExist(normalLocationEmissive) ? textureMap.registerSprite(normalLocationEmissive) : null;
            ResourceLocation activeLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_active_emissive", basePath, faceName));
            TextureAtlasSprite activeSpriteEmissive = ResourceHelper.isTextureExist(activeLocationEmissive) ? textureMap.registerSprite(activeLocationEmissive) : null;
            ResourceLocation pausedLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_paused_emissive", basePath, faceName));
            TextureAtlasSprite pausedSpriteEmissive = ResourceHelper.isTextureExist(pausedLocationEmissive) ? textureMap.registerSprite(pausedLocationEmissive) : null;
            sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, pausedSprite, normalSpriteEmissive, activeSpriteEmissive, pausedSpriteEmissive));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(OverlayFace.FRONT).getSprite(false, false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            ActivePredicate predicate = sprites.get(OverlayFace.bySide(renderSide, frontFacing));
            if (predicate != null) {

                TextureAtlasSprite renderSprite = predicate.getSprite(isActive, isWorkingEnabled);
                Textures.renderFace(renderState, translation, pipeline, renderSide, bounds, renderSprite);

                TextureAtlasSprite emissiveSprite = predicate.getEmissiveSprite(isActive, isWorkingEnabled);
                if (emissiveSprite != null) {
                    if (ConfigHolder.client.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                        Textures.renderFaceBloom(renderState, translation, lightPipeline, renderSide, bounds, emissiveSprite);
                    } else {
                        // have to still render both overlays or else textures will be broken
                        Textures.renderFace(renderState, translation, pipeline, renderSide, bounds, emissiveSprite);
                    }
                }
            }
        }
    }
}
