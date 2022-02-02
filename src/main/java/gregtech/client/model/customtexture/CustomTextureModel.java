package gregtech.client.model.customtexture;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gregtech.core.hooks.CTMHooks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class CustomTextureModel implements IModel {
    private final ModelBlock modelInfo;
    private final IModel vanillaModel;
    private Boolean uvLock;

    private final Collection<ResourceLocation> textureDependencies;
    private final Map<String, CustomTexture> textures = new HashMap<>();
    private transient byte layers;

    public CustomTextureModel(ModelBlock modelInfo, IModel vanillaModel) {
        this.modelInfo = modelInfo;
        this.vanillaModel = vanillaModel;
        this.textureDependencies = new HashSet<>();
        this.textureDependencies.addAll(vanillaModel.getTextures());
        this.textureDependencies.removeIf(rl -> rl.getPath().startsWith("#"));
    }

    public IModel getVanillaParent() {
        return vanillaModel;
    }

    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        boolean flag = (layers < 0 && state.getBlock().getRenderLayer() == layer) || ((layers >> layer.ordinal()) & 1) == 1;
        return CTMHooks.checkLayerWithOptiFine(flag, layers, layer);
    }

    @Override
    @ParametersAreNonnullByDefault
    @Nonnull
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel parent = vanillaModel.bake(state, format, rl -> {
            TextureAtlasSprite sprite = bakedTextureGetter.apply(rl);
            MetadataSectionCTM meta = null;
            try {
                meta = CustomTextureModelHandler.getMetadata(sprite);
            } catch (IOException ignored) {}
            MetadataSectionCTM finalMeta = meta;
            textures.computeIfAbsent(sprite.getIconName(), s -> {
                CustomTexture tex = new CustomTexture(finalMeta);
                layers |= 1 << (tex.getLayer() == null ? 7 : tex.getLayer().ordinal());
                return tex;
            });
            return sprite;
        });
        return new CustomTextureBakedModel(this, parent);
    }

    @Override
    @Nonnull
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    @Nonnull
    public Collection<ResourceLocation> getTextures() {
        return textureDependencies;
    }

    @Override
    @Nonnull
    public IModelState getDefaultState() {
        return getVanillaParent().getDefaultState();
    }

    @Override
    @Nonnull
    public Optional<? extends IClip> getClip(@Nonnull String name) {
        return getVanillaParent().getClip(name);
    }

    @Override
    @Nonnull
    public IModel process(@Nonnull ImmutableMap<String, String> customData) {
        return deepCopyOrMissing(getVanillaParent().process(customData), null, null);
    }

    @Override
    @Nonnull
    public IModel smoothLighting(boolean value) {
        if (modelInfo.isAmbientOcclusion() != value) {
            return deepCopyOrMissing(getVanillaParent().smoothLighting(value), value, null);
        }
        return this;
    }

    public CustomTexture getTexture(String iconName) {
        return textures.get(iconName);
    }

    @Override
    @Nonnull
    public IModel gui3d(boolean value) {
        if (modelInfo.isGui3d() != value) {
            return deepCopyOrMissing(getVanillaParent().gui3d(value), null, value);
        }
        return this;
    }

    @Override
    @Nonnull
    public IModel uvlock(boolean value) {
        if (uvLock == null || uvLock.booleanValue() != value) {
            IModel newParent = getVanillaParent().uvlock(value);
            if (newParent != getVanillaParent()) {
                IModel ret = deepCopyOrMissing(newParent, null, null);
                if (ret instanceof CustomTextureModel) {
                    ((CustomTextureModel) ret).uvLock = value;
                }
                return ret;
            }
        }
        return this;
    }

    @Override
    @Nonnull
    public IModel retexture(@Nonnull ImmutableMap<String, String> textures) {
        try {
            CustomTextureModel ret = deepCopy(getVanillaParent().retexture(textures), null, null);
            ret.modelInfo.textures.putAll(textures);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    private static final MethodHandle _asVanillaModel; static {
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().unreflect(IModel.class.getMethod("asVanillaModel"));
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
            mh = null;
        }
        _asVanillaModel = mh;
    }

    @Override
    @Nonnull
    public Optional<ModelBlock> asVanillaModel() {
        return Optional.ofNullable(_asVanillaModel)
                .<Optional<ModelBlock>>map(mh -> {
                    try {
                        return (Optional<ModelBlock>) mh.invokeExact(getVanillaParent());
                    } catch (Throwable e1) {
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .orElse(Optional.ofNullable(modelInfo));
    }

    private IModel deepCopyOrMissing(IModel newParent, Boolean ao, Boolean gui3d) {
        try {
            return deepCopy(newParent, ao, gui3d);
        } catch (IOException e) {
            e.printStackTrace();
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    private CustomTextureModel deepCopy(IModel newParent, Boolean ao, Boolean gui3d) throws IOException {
        // Deep copy logic taken from ModelLoader$VanillaModelWrapper
        List<BlockPart> parts = new ArrayList<>();
        for (BlockPart part : modelInfo.getElements()) {
            parts.add(new BlockPart(part.positionFrom, part.positionTo, Maps.newHashMap(part.mapFaces), part.partRotation, part.shade));
        }

        ModelBlock newModel = new ModelBlock(modelInfo.getParentLocation(), parts,
                Maps.newHashMap(modelInfo.textures), ao == null ? modelInfo.isAmbientOcclusion() : ao, gui3d == null ? modelInfo.isGui3d() : gui3d,
                modelInfo.getAllTransforms(), Lists.newArrayList(modelInfo.getOverrides()));

        newModel.name = modelInfo.name;
        newModel.parent = modelInfo.parent;
        return new CustomTextureModel(newModel, newParent);
    }
}
