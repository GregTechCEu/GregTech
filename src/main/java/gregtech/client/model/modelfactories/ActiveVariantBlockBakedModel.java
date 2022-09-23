package gregtech.client.model.modelfactories;

import gregtech.api.block.VariantActiveBlock;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.block.VariantActiveBlock.GLOW;
import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ActiveVariantBlockBakedModel implements IBakedModel {
    private final ThreadLocal<TextureAtlasSprite> particle;

    ActiveVariantBlockBakedModel() {
        this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = Collections.emptyList();
        if (side == null) return quads;
        if (state != null) {
            ModelResourceLocation mrl;
            if (((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE)) {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=true," + statePropertiesToString(state.getProperties()));
            } else {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=false," + statePropertiesToString(state.getProperties()));
            }
            IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager().getModel(mrl);
            TextureAtlasSprite textureAtlasSprite = m.getParticleTexture();
            particle.set(textureAtlasSprite);
            if (MinecraftForgeClient.getRenderLayer() == BloomEffectUtil.BLOOM) {
                quads = new ArrayList<>();
                for (BakedQuad b : m.getQuads(state, side, rand) ) {
                    if (b.getSprite().getIconName().contains("bloom")) {
                        quads.add(b);
                        GLOW.put(state,true);
                    }
                }
            } else {
                quads = new ArrayList<>(m.getQuads(state, side, rand));
            }
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        if (Minecraft.getMinecraft().world != null ) {
            ModelResourceLocation mrl;
            if (((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE)) {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=true," + statePropertiesToString(state.getProperties()));
            } else {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=false," + statePropertiesToString(state.getProperties()));
            }
            IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager().getModel(mrl);
            return m.isAmbientOcclusion();
        }
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particle.get();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
