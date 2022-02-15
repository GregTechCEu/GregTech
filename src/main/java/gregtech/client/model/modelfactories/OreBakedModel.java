package gregtech.client.model.modelfactories;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import gregtech.client.model.ModelFactory;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.client.model.customtexture.CustomTexture;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.blocks.BlockOre;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;

@SideOnly(Side.CLIENT)
public class OreBakedModel implements IBakedModel {

    private static IBakedModel model;

    private final Map<StoneType, TextureAtlasSprite> stoneTypeModels;
    private final Table<StoneType, EnumFacing, BakedQuad> cacheBottom;
    private final Table<MaterialIconSet, EnumFacing, BakedQuad[]> cacheTop;
    private final ThreadLocal<TextureAtlasSprite> particle;

   public OreBakedModel() {
       this.stoneTypeModels = new Object2ObjectOpenHashMap<>();
       this.cacheBottom = Tables.newCustomTable(Maps.newHashMap(), () -> Maps.newEnumMap(EnumFacing.class));
       this.cacheTop = Tables.newCustomTable(Maps.newHashMap(), () -> Maps.newEnumMap(EnumFacing.class));
       this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
   }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state != null) {
            BlockOre ore = (BlockOre) state.getBlock();
            StoneType stoneType = state.getValue(ore.STONE_TYPE);
            MaterialIconSet materialIconSet = ore.material.getMaterialIconSet();
            TextureAtlasSprite textureAtlasSprite = stoneTypeModels.computeIfAbsent(stoneType, type-> Minecraft.getMinecraft().blockRenderDispatcher.getModelForState(type.stone.get()).getParticleTexture());
            particle.set(textureAtlasSprite);
            if (model == null) {
                model = new ModelFactory(ModelFactory.ModelTemplate.CUBE_2_LAYER_ALL_TINT_INDEX).bake();
            }
            BakedQuad bakedQuad = cacheBottom.get(stoneType, side);
            if (side == null) return Collections.emptyList();
            if (bakedQuad == null) {
                cacheBottom.put(stoneType, side, bakedQuad = new BakedQuadRetextured(model.getQuads(state, side, rand).get(0), textureAtlasSprite));
            }
            BakedQuad[] bakedQuads =  cacheTop.get(materialIconSet, side);
            if (bakedQuads == null) {
                bakedQuads = new BakedQuad[2];
                bakedQuads[0] = new BakedQuadRetextured(model.getQuads(state, side, rand).get(1), ModelLoader.defaultTextureGetter().apply(MaterialIconType.ore.getBlockPath(materialIconSet)));
                bakedQuads[1] = CustomTexture.rebake(15, 15, bakedQuads[0]);
                cacheTop.put(materialIconSet, side, bakedQuads);
            }
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            boolean hasEmissive = ore.material.getProperty(PropertyKey.ORE).isEmissive();
            boolean isEmissiveLayer = hasEmissive && (layer == BloomEffectUtil.getRealBloomLayer() || layer == null);
            List<BakedQuad> quads = new ArrayList<>();
            if (!hasEmissive || !isEmissiveLayer || layer == null) {
                quads.add(bakedQuad);
            }
            if (hasEmissive && isEmissiveLayer) {
                quads.add(bakedQuads[1]);
            } else {
                quads.add(bakedQuads[0]);
            }
            return quads;
        } else {
            List<BakedQuad> quads = new ArrayList<>();
            ItemStack stack = OreItemOverride.INSTANCE.stack.get();
            if (!stack.isEmpty()) {
                BlockOre ore = (BlockOre) ((ItemBlock) stack.getItem()).getBlock();
                IBlockState oreState = ore.getDefaultState().withProperty(ore.STONE_TYPE, ore.STONE_TYPE.getAllowedValues().get(stack.getMetadata()));
                for (EnumFacing face : EnumFacing.VALUES) {
                    quads.addAll(getQuads(oreState, face, rand));
                }
            }
            return quads;
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
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
    public TextureAtlasSprite getParticleTexture() {
        return particle.get();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        TRSRTransformation blockTransform = ModelFactory.getBlockTransform(cameraTransformType);
        return blockTransform == null ? IBakedModel.super.handlePerspective(cameraTransformType) : Pair.of(this, blockTransform.getMatrix());
    }

    @Override
    public ItemOverrideList getOverrides() {
        return OreItemOverride.INSTANCE;
    }

    private static class OreItemOverride extends ItemOverrideList {

        private static final OreItemOverride INSTANCE = new OreItemOverride();

        private final ThreadLocal<ItemStack> stack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            this.stack.set(stack);
            return originalModel;
        }
    }
}
