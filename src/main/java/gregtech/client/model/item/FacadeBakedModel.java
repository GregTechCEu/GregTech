package gregtech.client.model.item;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import gregtech.api.model.ModelFactory;
import gregtech.api.util.ModCompatibility;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.behaviors.FacadeItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FacadeBakedModel implements IBakedModel {

    public static final FacadeBakedModel INSTANCE = new FacadeBakedModel();

    private final Map<ItemCameraTransforms.TransformType, TRSRTransformation> transformationMap = new EnumMap<>(ItemCameraTransforms.TransformType.class);

    private LoadingCache<IBakedModel, List<BakedQuad>> cachedQuads;

    private FacadeBakedModel() {
        transformationMap.put(ItemCameraTransforms.TransformType.GUI, ModelFactory.getTransform(0, 0, 0, 30, -149, 0, 0.8f));
        transformationMap.put(ItemCameraTransforms.TransformType.GROUND, ModelFactory.getTransform(0, -1, 0, 0, 0, 0, 0.35f));
        transformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, ModelFactory.getTransform(0, 2.5f, 0, 0, -180, 0, 0.4f));
        transformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, ModelFactory.getTransform(0.25f, -2.75f, 1.25f, 75f, 0, 0, 0.4f));
        transformationMap.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, ModelFactory.getTransform(0, 2.5f, 0, 0, -180, 0, 0.4f));
        transformationMap.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, ModelFactory.getTransform(0.25f, -2.75f, 1.25f, 75f, 0, 0, 0.4f));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (cachedQuads == null) {
            Function<IBakedModel, List<BakedQuad>> func = (ibm) -> {
                ItemStack stack = FacadeItemOverride.INSTANCE.stack.get();
                IBlockState modelBlockState = FacadeHelper.lookupBlockForItem(stack);
                List<BakedQuad> quads = new ArrayList<>();
                for (BakedQuad quad : ibm.getQuads(modelBlockState, EnumFacing.NORTH, 0)) {
                    int colour = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, quad.getTintIndex());
                    for (BakedQuad itemQuad : ItemLayerModel.getQuadsForSprite(-1, quad.getSprite(), quad.getFormat(), Optional.empty())) {
                        if (colour != -1) {
                            int[] tintedVertexData = itemQuad.getVertexData().clone();
                            VertexFormat format = itemQuad.getFormat();
                            for (int i = 0; i < 4; i++) {
                                tintedVertexData[(format.getColorOffset() / 4) + format.getIntegerSize() * i] = rgbToABGR(colour);
                            }
                            itemQuad = new BakedQuad(tintedVertexData, -1, itemQuad.getFace(), itemQuad.getSprite(), false, format);
                        } else {
                            itemQuad = new BakedQuad(itemQuad.getVertexData(), -1, itemQuad.getFace(), itemQuad.getSprite(), false, itemQuad.getFormat());
                        }
                        quads.add(itemQuad);
                    }
                }
                return quads;
            };
            cachedQuads = CacheBuilder.newBuilder().initialCapacity(100).expireAfterAccess(20, TimeUnit.SECONDS).build(CacheLoader.from(func));
        }
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = FacadeItemOverride.INSTANCE.stack.get();
        IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, mc.world, mc.player);
        return cachedQuads.getUnchecked(model);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, transformationMap.get(cameraTransformType).getMatrix());
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return ModelLoader.White.INSTANCE;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return FacadeItemOverride.INSTANCE;
    }

    private List<BakedQuad> tintQuads(List<BakedQuad> quads) {
        List<BakedQuad> tintedQuads = new ArrayList<>();
        for (BakedQuad quad : quads) {
            BakedQuad tintedQuad;
            int colour = Minecraft.getMinecraft().getItemColors().colorMultiplier(FacadeItemOverride.INSTANCE.stack.get(), quad.getTintIndex());
            if (colour != -1) {
                int[] tintedVertexData = quad.getVertexData().clone();
                VertexFormat format = quad.getFormat();
                for (int i = 0; i < 4; i++) {
                    tintedVertexData[(format.getColorOffset() / 4) + format.getIntegerSize() * i] = rgbToABGR(colour);
                }
                tintedQuad = new BakedQuad(tintedVertexData, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), format);
            } else {
                tintedQuad = quad;
            }
            tintedQuads.add(tintedQuad);
            tintedQuads.add(new BakedQuad(tintedQuad.getVertexData(), tintedQuad.getTintIndex(), EnumFacing.SOUTH, tintedQuad.getSprite(), tintedQuad.shouldApplyDiffuseLighting(), tintedQuad.getFormat()));
        }
        return tintedQuads;
    }

    private int rgbToABGR(int rgb) {
        rgb |= 0xFF000000;
        int r = (rgb >> 16) & 0xFF;
        int b = rgb & 0xFF;
        return (rgb & 0xFF00FF00) | (b << 16) | r;
    }

    private static class FacadeItemOverride extends ItemOverrideList {

        private static final FacadeItemOverride INSTANCE = new FacadeItemOverride();

        private final ThreadLocal<ItemStack> stack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            this.stack.set(FacadeItem.getFacadeStack(ModCompatibility.getRealItemStack(stack)));
            return originalModel;
        }

    }

}
