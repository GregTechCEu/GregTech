package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
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
    private Map<OverlayFace, ActivePredicate> sprites;

    @SideOnly(Side.CLIENT)
    private static class ActivePredicate {

        private final TextureAtlasSprite normalSprite;
        private final TextureAtlasSprite activeSprite;

        public ActivePredicate(TextureAtlasSprite normalSprite, TextureAtlasSprite activeSprite) {
            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
        }

        public TextureAtlasSprite getSprite(boolean active) {
            return active ? activeSprite : normalSprite;
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
        this.sprites = new HashMap<>();
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation normalLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s", basePath, faceName));
            ResourceLocation activeLocation = new ResourceLocation(GTValues.MODID, String.format("blocks/%s/overlay_%s_active", basePath, faceName));
            TextureAtlasSprite normalSprite = textureMap.registerSprite(normalLocation);
            TextureAtlasSprite activeSprite = textureMap.registerSprite(activeLocation);
            sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite));
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            OverlayFace overlayFace = OverlayFace.bySide(renderSide, frontFacing);
            if (sprites.containsKey(overlayFace)) {
                TextureAtlasSprite renderSprite = sprites.get(overlayFace).getSprite(isActive);
                Textures.renderFace(renderState, translation, ops, renderSide, bounds, renderSprite);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, boolean isActive) {
        render(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive);
    }
}
