package gregtech.client.model.customtexture;

import gregtech.api.util.GTLog;
import gregtech.asm.hooks.BlockHooks;
import gregtech.asm.hooks.CTMHooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4f;

@SideOnly(Side.CLIENT)
public class CustomTextureBakedModel implements IBakedModel {

    private final CustomTextureModel model;
    private final IBakedModel parent;

    public static final Cache<CustomTextureBakedModel.State, CustomTextureBakedModel> MODEL_CACHE = CacheBuilder
            .newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();

    protected final ListMultimap<BlockRenderLayer, BakedQuad> genQuads = MultimapBuilder
            .enumKeys(BlockRenderLayer.class).arrayListValues().build();
    protected final Table<BlockRenderLayer, EnumFacing, List<BakedQuad>> faceQuads = Tables
            .newCustomTable(Maps.newEnumMap(BlockRenderLayer.class), () -> Maps.newEnumMap(EnumFacing.class));

    private final EnumMap<EnumFacing, ImmutableList<BakedQuad>> noLayerCache = new EnumMap<>(EnumFacing.class);
    private ImmutableList<BakedQuad> noSideNoLayerCache;

    public CustomTextureBakedModel(CustomTextureModel model, IBakedModel parent) {
        this.model = model;
        this.parent = parent;
    }

    public IBakedModel getParent(long rand) {
        if (parent instanceof WeightedBakedModel) {
            return ((WeightedBakedModel) parent).getRandomModel(rand);
        }
        return parent;
    }

    public CustomTextureModel getModel() {
        return model;
    }

    protected CustomTexture getTexture(long rand, String iconName) {
        CustomTexture ret = getModel().getTexture(iconName);
        if (ret == null) {
            ret = applyToParent(rand, parent -> parent.getTexture(rand, iconName));
        }
        return ret;
    }

    protected TextureAtlasSprite getOverrideSprite(long rand, int tintIndex) {
        return applyToParent(rand, parent -> parent.getOverrideSprite(rand, tintIndex));
    }

    private <T> T applyToParent(long rand, Function<CustomTextureBakedModel, T> func) {
        IBakedModel parent = getParent(rand);
        if (parent instanceof CustomTextureBakedModel) {
            return func.apply((CustomTextureBakedModel) parent);
        }
        return null;
    }

    protected CustomTextureBakedModel createModel(@Nullable IBlockState state, CustomTextureModel model, long rand) {
        IBakedModel parent = getParent(rand);
        while (parent instanceof CustomTextureBakedModel) {
            parent = ((CustomTextureBakedModel) parent).getParent(rand);
        }

        CustomTextureBakedModel ret = new CustomTextureBakedModel(model, parent);
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                List<BakedQuad> parentQuads = parent.getQuads(state, facing, rand);
                List<BakedQuad> quads;
                if (facing != null) {
                    ret.faceQuads.put(layer, facing, quads = new LinkedList<>());
                } else {
                    quads = ret.genQuads.get(layer);
                }

                // Linked to maintain the order of quads
                Map<BakedQuad, CustomTexture> textureMap = new LinkedHashMap<>();
                // Gather all quads and map them to their textures
                // All quads should have an associated ICTMTexture, so ignore any that do not
                for (BakedQuad q : parentQuads) {
                    CustomTexture tex = this.getTexture(rand, q.getSprite().getIconName());
                    if (tex != null) {
                        TextureAtlasSprite spriteReplacement = this.getOverrideSprite(rand, q.getTintIndex());
                        if (spriteReplacement != null) {
                            q = new BakedQuadRetextured(q, spriteReplacement);
                        }
                        textureMap.put(q, tex);
                    }
                }

                BlockHooks.ENABLE = false;
                for (Map.Entry<BakedQuad, CustomTexture> e : textureMap.entrySet()) {
                    // If the layer is null, this is a wrapped vanilla texture, so passthrough the layer check to the
                    // block
                    if (e.getValue().getLayer() == layer || (e.getValue().getLayer() == null &&
                            (state == null || state.getBlock().canRenderInLayer(state, layer)))) {
                        quads.add(e.getValue().transformQuad(e.getKey()));
                    }
                }
                BlockHooks.ENABLE = true;
            }
        }
        return ret;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        IBakedModel parent = getParent(rand);

        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        CustomTextureBakedModel baked;
        try {
            baked = MODEL_CACHE.get(new State(state, parent), () -> createModel(state, model, rand));
            List<BakedQuad> ret;
            if (side != null && layer != null) {
                ret = baked.faceQuads.get(layer, side);
            } else if (side != null) {
                ret = baked.noLayerCache.computeIfAbsent(side,
                        f -> ImmutableList.copyOf(baked.faceQuads.column(f).values()
                                .stream()
                                .flatMap(List::stream)
                                .distinct()
                                .collect(Collectors.toList())));
            } else if (layer != null) {
                ret = baked.genQuads.get(layer);
            } else {
                ret = baked.noSideNoLayerCache;
                if (ret == null) {
                    ret = baked.noSideNoLayerCache = ImmutableList.copyOf(baked.genQuads.values()
                            .stream()
                            .distinct()
                            .collect(Collectors.toList()));
                }
            }
            return CTMHooks.getQuadsWithOptiFine(ret, layer, this, state, side, rand);
        } catch (ExecutionException e) {
            GTLog.logger.error("Could not retrieve BakedModel from cache", e);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return parent.isBuiltInRenderer();
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Override
    @NotNull
    public ItemOverrideList getOverrides() {
        return parent.getOverrides();
    }

    @Override
    @NotNull
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@NotNull ItemCameraTransforms.TransformType cameraTransformType) {
        return parent.handlePerspective(cameraTransformType);
    }

    private static class State {

        private final IBlockState cleanState;
        private final IBakedModel parent;

        public State(IBlockState cleanState, IBakedModel parent) {
            this.cleanState = cleanState;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomTextureBakedModel.State other = (CustomTextureBakedModel.State) obj;

            if (cleanState != other.cleanState) {
                return false;
            }
            return parent == other.parent;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // for some reason blockstates hash their properties, we only care about the identity hash
            result = prime * result + System.identityHashCode(cleanState);
            result = prime * result + (parent == null ? 0 : parent.hashCode());
            return result;
        }
    }
}
