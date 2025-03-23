package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.client.renderer.GTRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.RenderContext;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.blocks.BlockWireCoil;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

public class WireCoilRenderer extends SimpleCubeRenderer {

    TextureAtlasSprite base;
    TextureAtlasSprite emissive;

    public WireCoilRenderer(String basePath) {
        super(basePath);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        String modID = GTValues.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        this.base = ICubeRenderer.getResourceSafe(textureMap, modID, "blocks/" + basePath + "_base");
        this.sprite = ICubeRenderer.getResourceSafe(textureMap, modID, "blocks/" + basePath);
        this.emissive = ICubeRenderer.getResourceSafe(textureMap, modID, "blocks/" + basePath + "_bloom");
    }

    @Override
    public void render(GTRendererState rendererState, RenderContext context) {
        IExtendedBlockState state = context.getExtendedState();
        boolean active = state != null && state.getValue(VariantActiveBlock.ACTIVE);
        for (EnumFacing side : EnumFacing.VALUES) {
            renderOrientedState(rendererState, context, side, active, false);
        }
    }

    @Override
    public void renderOrientedState(GTRendererState rendererState, RenderContext context,
                                    EnumFacing face, boolean isActive, boolean isWorkingEnabled) {
        BlockRenderLayer layer = isGeneric(context) ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID;

        rendererState.setTexture(this.base);
        rendererState.quad(face, layer);
        rendererState.setTexture(this.sprite);
        rendererState.quad(face, layer);
        if (isActive) {
            rendererState.setTexture(this.emissive);
            rendererState.quad(face, BloomEffectUtil.getEffectiveBloomLayer());
        }
    }

    private static boolean isGeneric(RenderContext context) {
        if (!(context.state.getBlock() instanceof BlockWireCoil wireCoil)) return false;
        return wireCoil.getState(context.state).generic;
    }
}
