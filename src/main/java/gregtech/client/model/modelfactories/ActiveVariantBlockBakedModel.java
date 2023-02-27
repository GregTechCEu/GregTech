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
import java.util.stream.Collectors;

import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ActiveVariantBlockBakedModel implements IBakedModel {
    private final ThreadLocal<TextureAtlasSprite> particle;

    ActiveVariantBlockBakedModel() {
        this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads;
        if (side == null || state == null) return Collections.emptyList();
        ModelResourceLocation mrl;
        boolean activeState;
        //Some mods like to call this without getting the extendedBlockState leading to a NPE crash since the
        //unlisted ACTIVE property is null.
        if (((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE) == null) {
            activeState = false;
        } else {
            activeState = ((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE);
        }

        ResourceLocation registryName = state.getBlock().getRegistryName();
        if (registryName == null) {
            throw new IllegalArgumentException("Block " + state.getBlock().getTranslationKey() + " has null registry name");
        }
        if (activeState) {
            mrl = new ModelResourceLocation(registryName,
                    "active=true,variant=" + state.getProperties().entrySet().stream().filter(p -> p.getKey().getName().equals("variant")).map(e -> {
                        IProperty<?> p = e.getKey();
                        return getPropertyName(p, e.getValue());
                    }).collect(Collectors.joining()));
        } else {
            mrl = new ModelResourceLocation(registryName,
                    "active=false,variant=" + state.getProperties().entrySet().stream().filter(p -> p.getKey().getName().equals("variant")).map(e -> {
                        IProperty<?> p = e.getKey();
                        return getPropertyName(p, e.getValue());
                    }).collect(Collectors.joining()));
        }
        IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager().getModel(mrl);
        TextureAtlasSprite textureAtlasSprite = m.getParticleTexture();
        particle.set(textureAtlasSprite);
        if (MinecraftForgeClient.getRenderLayer() == BloomEffectUtil.getRealBloomLayer()) {
            if (ConfigHolder.client.casingsActiveEmissiveTextures) {
                quads = new ArrayList<>();
                for (BakedQuad b : m.getQuads(state, side, rand)) {
                    if (b.getSprite().getIconName().contains("bloom")) {
                        quads.add(b);
                    }
                }
            }
            else {
                quads = Collections.emptyList();
            }
        } else {
            quads = new ArrayList<>(m.getQuads(state, side, rand));
        }
        return quads;
    }

    public static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isAmbientOcclusion(@Nonnull IBlockState state) {
        if (Minecraft.getMinecraft().world != null ) {
            ResourceLocation registryName = state.getBlock().getRegistryName();
            if (registryName == null) {
                throw new IllegalArgumentException("Block " + state.getBlock().getTranslationKey() +
                        " has null registry name");
            }

            ModelResourceLocation mrl;
            if (((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE)) {
                mrl = new ModelResourceLocation(registryName,
                        "active=true," + statePropertiesToString(state.getProperties()));
            } else {
                mrl = new ModelResourceLocation(registryName,
                        "active=false," + statePropertiesToString(state.getProperties()));
            }
            IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes()
                    .getModelManager().getModel(mrl);
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
