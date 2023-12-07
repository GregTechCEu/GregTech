package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class OrientedOverlayRenderer implements ICubeRenderer {

    public enum OverlayFace {

        FRONT,
        BACK,
        TOP,
        BOTTOM,
        SIDE;

        public static final OverlayFace[] VALUES = values();

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

        public ActivePredicate(@NotNull TextureAtlasSprite normalSprite,
                               @NotNull TextureAtlasSprite activeSprite,
                               @Nullable TextureAtlasSprite pausedSprite,
                               @Nullable TextureAtlasSprite normalSpriteEmissive,
                               @Nullable TextureAtlasSprite activeSpriteEmissive,
                               @Nullable TextureAtlasSprite pausedSpriteEmissive) {
            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.normalSpriteEmissive = normalSpriteEmissive;
            this.activeSpriteEmissive = activeSpriteEmissive;
            this.pausedSpriteEmissive = pausedSpriteEmissive;
        }

        public @Nullable TextureAtlasSprite getSprite(boolean active, boolean workingEnabled) {
            if (active) {
                if (workingEnabled) {
                    return activeSprite;
                } else if (pausedSprite != null) {
                    return pausedSprite;
                }
            }
            return normalSprite;
        }

        public @Nullable TextureAtlasSprite getEmissiveSprite(boolean active, boolean workingEnabled) {
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

    public OrientedOverlayRenderer(@NotNull String basePath) {
        this.basePath = basePath;
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

        boolean foundTexture = false;
        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            final String faceName = overlayFace.name().toLowerCase();
            final String overlayPath = String.format("blocks/%s/overlay_%s", basePath, faceName);

            // if a normal texture location is found, try to find the rest
            TextureAtlasSprite normalSprite = ICubeRenderer.getResource(textureMap, modID, overlayPath);
            // require the normal texture to get the rest
            if (normalSprite == null) continue;

            foundTexture = true;

            // normal

            final String active = String.format("%s_active", overlayPath);
            TextureAtlasSprite activeSprite = ICubeRenderer.getResource(textureMap, modID, active);

            if (activeSprite == null) {
                FMLClientHandler.instance().trackMissingTexture(new ResourceLocation(modID,
                        "blocks/" + basePath + "/overlay_" + overlayFace.toString().toLowerCase() + "_active"));
                continue;
            }

            final String paused = String.format("%s_paused", overlayPath);
            TextureAtlasSprite pausedSprite = ICubeRenderer.getResource(textureMap, modID, paused);

            // emissive

            TextureAtlasSprite normalSpriteEmissive = ICubeRenderer.getResource(textureMap, modID,
                    overlayPath + EMISSIVE);

            TextureAtlasSprite activeSpriteEmissive = ICubeRenderer.getResource(textureMap, modID, active + EMISSIVE);

            TextureAtlasSprite pausedSpriteEmissive = ICubeRenderer.getResource(textureMap, modID, paused + EMISSIVE);

            sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, pausedSprite,
                    normalSpriteEmissive, activeSpriteEmissive, pausedSpriteEmissive));
        }

        if (!foundTexture) {
            FMLClientHandler.instance()
                    .trackMissingTexture(new ResourceLocation(modID, "blocks/" + basePath + "/overlay_OVERLAY_FACE"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        for (OrientedOverlayRenderer.ActivePredicate predicate : sprites.values()) {
            TextureAtlasSprite sprite = predicate.getSprite(false, false);
            if (sprite != null) return sprite;
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {

            ActivePredicate predicate = sprites.get(OverlayFace.bySide(renderSide, frontFacing));
            if (predicate != null) {
                TextureAtlasSprite renderSprite = predicate.getSprite(isActive, isWorkingEnabled);

                // preserve the original translation when not rotating the top and bottom
                Matrix4 renderTranslation = translation.copy();

                // Rotate the top and bottom faces to match front facing
                Rotation rotation = getRotation(renderTranslation, renderSide, frontFacing);
                renderTranslation = RenderUtil.adjustTrans(renderTranslation, renderSide, 1);
                renderTranslation.apply(rotation);

                Textures.renderFace(renderState, renderTranslation, ArrayUtils.addAll(pipeline, rotation), renderSide,
                        bounds, renderSprite, BlockRenderLayer.CUTOUT_MIPPED);

                TextureAtlasSprite emissiveSprite = predicate.getEmissiveSprite(isActive, isWorkingEnabled);
                if (emissiveSprite != null) {
                    if (ConfigHolder.client.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240),
                                rotation);
                        Textures.renderFace(renderState, renderTranslation, lightPipeline, renderSide, bounds,
                                emissiveSprite, BloomEffectUtil.getEffectiveBloomLayer());
                    } else {
                        // have to still render both overlays or else textures will be broken
                        Textures.renderFace(renderState, renderTranslation, ArrayUtils.addAll(pipeline, rotation),
                                renderSide, bounds, emissiveSprite, BlockRenderLayer.CUTOUT_MIPPED);
                    }
                }
            }
        }
    }

    public Rotation getRotation(Matrix4 transformation, EnumFacing renderSide, EnumFacing frontFacing) {
        Rotation rotation = new Rotation(0, 0, 1, 0);
        if (renderSide.getAxis() == EnumFacing.Axis.Y) {
            if (frontFacing == EnumFacing.NORTH) {
                transformation.translate(1, 0, 1);
                rotation = new Rotation(Math.PI, 0, 1, 0);
            } else if (frontFacing == EnumFacing.EAST) {
                transformation.translate(0, 0, 1);
                rotation = new Rotation(Math.PI / 2, 0, 1, 0);
            } else if (frontFacing == EnumFacing.WEST) {
                transformation.translate(1, 0, 0);
                rotation = new Rotation(-Math.PI / 2, 0, 1, 0);
            }
        }
        return rotation;
    }
}
