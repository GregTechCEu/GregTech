package gregtech.client.renderer.texture.cube;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.client.renderer.GTRendererState;
import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class WireCoilRenderer extends SimpleCubeRenderer {

    TextureAtlasSprite base;
    TextureAtlasSprite emissive;

    public WireCoilRenderer(String basePath) {
        super(basePath);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        super.registerIcons(textureMap);
        String modID = GTValues.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        this.base = textureMap.registerSprite(new ResourceLocation(modID, basePath + "_base"));
        this.emissive = textureMap.registerSprite(new ResourceLocation(modID, basePath + "_bloom"));
    }

    @Override
    public void render(GTRendererState rendererState) {
        Boolean active = ((IExtendedBlockState) rendererState.state).getValue(VariantActiveBlock.ACTIVE);
        for (EnumFacing side : EnumFacing.values()) {
            renderOrientedState(rendererState, side, Boolean.TRUE.equals(active), false);
        }
    }

    @Override
    public void renderOrientedState(GTRendererState rendererState, EnumFacing face, boolean isActive,
                                    boolean isWorkingEnabled) {
        rendererState.setTexture(this.base);
        rendererState.quad(face, BlockRenderLayer.SOLID);
        rendererState.setTexture(this.sprite);
        rendererState.quad(face, BlockRenderLayer.SOLID);
        if (isActive) {
            rendererState.setTexture(this.emissive);
            rendererState.quad(face, BloomEffectUtil.getEffectiveBloomLayer());
        }
    }
}
