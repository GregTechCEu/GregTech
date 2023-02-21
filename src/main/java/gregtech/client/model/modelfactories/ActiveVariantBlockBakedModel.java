package gregtech.client.model.modelfactories;

import gregtech.api.block.VariantActiveBlock;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveVariantBlockBakedModel implements IBakedModel {

    private final ThreadLocal<TextureAtlasSprite> particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());

    protected boolean isBloomEnabled() {
        return ConfigHolder.client.casingsActiveEmissiveTextures;
    }

    @Nullable
    protected ModelResourceLocation getModelLocation(IBlockState state) {
        ResourceLocation registryName = state.getBlock().getRegistryName();
        if (registryName == null) {
            return null;
        }

        //Some mods like to call this without getting the extendedBlockState leading to a NPE crash since the
        //unlisted ACTIVE property is null.
        boolean active = Boolean.TRUE.equals(((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE));

        return new ModelResourceLocation(registryName,
                "active=" + active + ",variant=" + state.getProperties().entrySet().stream()
                        .filter(p -> p.getKey().getName().equals("variant"))
                        .map(e -> getPropertyName(e.getKey(), e.getValue()))
                        .findFirst().orElse("invalid"));
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) return Collections.emptyList();

        ModelResourceLocation mrl = getModelLocation(state);
        if (mrl == null) {
            return Collections.emptyList();
        }

        IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager().getModel(mrl);
        particle.set(m.getParticleTexture());

        if (MinecraftForgeClient.getRenderLayer() != BloomEffectUtil.getRealBloomLayer()) {
            return m.getQuads(state, side, rand);
        } else if (isBloomEnabled()) {
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedQuad b : m.getQuads(state, side, rand)) {
                if (b.getSprite().getIconName().contains("bloom")) {
                    quads.add(b);
                }
            }
            return quads;
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isAmbientOcclusion(@Nonnull IBlockState state) {
        if (Minecraft.getMinecraft().world == null) {
            return true;
        }
        ModelResourceLocation mrl = getModelLocation(state);
        if (mrl == null) {
            return true;
        }
        IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes()
                .getModelManager().getModel(mrl);
        return m.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particle.get();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
