package gregtech.client.model.customtexture;

import gregtech.api.util.GTLog;
import gregtech.asm.hooks.CTMHooks;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

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
        boolean canRenderInLayer = (layers < 0 && state.getBlock().getRenderLayer() == layer) ||
                ((layers >> layer.ordinal()) & 1) == 1;
        return CTMHooks.checkLayerWithOptiFine(canRenderInLayer, layers, layer);
    }

    @Override
    @NotNull
    public IBakedModel bake(@NotNull IModelState state, @NotNull VertexFormat format,
                            @NotNull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
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
    @NotNull
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    @NotNull
    public Collection<ResourceLocation> getTextures() {
        return textureDependencies;
    }

    @Override
    @NotNull
    public IModelState getDefaultState() {
        return getVanillaParent().getDefaultState();
    }

    @Override
    @NotNull
    public Optional<? extends IClip> getClip(@NotNull String name) {
        return getVanillaParent().getClip(name);
    }

    @Override
    @NotNull
    public IModel process(@NotNull ImmutableMap<String, String> customData) {
        return deepCopyOrMissing(getVanillaParent().process(customData), null, null);
    }

    @Override
    @NotNull
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
    @NotNull
    public IModel gui3d(boolean value) {
        if (modelInfo.isGui3d() != value) {
            return deepCopyOrMissing(getVanillaParent().gui3d(value), null, value);
        }
        return this;
    }

    @Override
    @NotNull
    public IModel uvlock(boolean value) {
        if (uvLock == null || uvLock != value) {
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
    @NotNull
    public IModel retexture(@NotNull ImmutableMap<String, String> textures) {
        try {
            CustomTextureModel ret = deepCopy(getVanillaParent().retexture(textures), null, null);
            ret.modelInfo.textures.putAll(textures);
            return ret;
        } catch (IOException e) {
            GTLog.logger.error("Could not create CustomTextureModel texture deep copy", e);
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    private static final MethodHandle _asVanillaModel;
    static {
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().unreflect(IModel.class.getMethod("asVanillaModel"));
        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
            mh = null;
        }
        _asVanillaModel = mh;
    }

    @Override
    @NotNull
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
            GTLog.logger.error("Could not create texture deep copy", e);
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    private CustomTextureModel deepCopy(IModel newParent, Boolean ao, Boolean gui3d) throws IOException {
        // Deep copy logic taken from ModelLoader$VanillaModelWrapper
        List<BlockPart> parts = new ArrayList<>();
        for (BlockPart part : modelInfo.getElements()) {
            parts.add(new BlockPart(part.positionFrom, part.positionTo, Maps.newHashMap(part.mapFaces),
                    part.partRotation, part.shade));
        }

        ModelBlock newModel = new ModelBlock(modelInfo.getParentLocation(), parts,
                Maps.newHashMap(modelInfo.textures), ao == null ? modelInfo.isAmbientOcclusion() : ao,
                gui3d == null ? modelInfo.isGui3d() : gui3d,
                modelInfo.getAllTransforms(), Lists.newArrayList(modelInfo.getOverrides()));

        newModel.name = modelInfo.name;
        newModel.parent = modelInfo.parent;
        return new CustomTextureModel(newModel, newParent);
    }
}
