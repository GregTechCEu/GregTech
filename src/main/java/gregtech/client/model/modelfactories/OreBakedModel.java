package gregtech.client.model.modelfactories;

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
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;

public class OreBakedModel implements IBakedModel {

    public static final OreBakedModel INSTANCE = new OreBakedModel();

    private final Map<StoneType, IBakedModel> stoneTypeModels;
    private final Map<MaterialIconSet, Map<EnumFacing, BakedQuad>> materialFaces;
    private final ThreadLocal<TextureAtlasSprite> particle;

   private OreBakedModel() {
       this.stoneTypeModels = new Object2ObjectOpenHashMap<>();
       this.materialFaces = new Object2ObjectOpenHashMap<>();
       this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
   }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();
        if (state != null) {
            BlockOre ore = (BlockOre) state.getBlock();
            StoneType stoneType = state.getValue(ore.STONE_TYPE);
            IBakedModel stoneTypeModel = stoneTypeModels.get(stoneType);
            if (stoneTypeModel == null) {
                stoneTypeModels.put(stoneType, stoneTypeModel = Minecraft.getMinecraft().blockRenderDispatcher.getModelForState(stoneType.stone.get()));
            }
            particle.set(stoneTypeModel.getParticleTexture());
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            boolean hasEmissive = ore.material.getProperty(PropertyKey.ORE).isEmissive();
            boolean isEmissiveLayer = hasEmissive && (layer == BloomEffectUtil.getRealBloomLayer() || layer == null);
            if (!hasEmissive || !isEmissiveLayer || layer == null) {
                quads.addAll(stoneTypeModel.getQuads(stoneType.stone.get(), side, rand));
            }
            if (hasEmissive && !isEmissiveLayer) {
                return quads;
            }
            Map<EnumFacing, BakedQuad> materialFace =  materialFaces.get(ore.material.getMaterialIconSet());
            if (materialFace == null) {
                materialFaces.put(ore.material.getMaterialIconSet(), materialFace = new Object2ObjectOpenHashMap<>());
            }
            side = side == null ? EnumFacing.NORTH : side;
            BakedQuad materialFaceQuad = materialFace.get(side);
            if (materialFaceQuad == null) {
                materialFaceQuad = ModelFactory.getBakery().makeBakedQuad(
                        new Vector3f(0, 0, 0),
                        new Vector3f(16, 16, 16),
                        new BlockPartFace(side, 1, "", new BlockFaceUV(new float[] { 0.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.0F, 16.0F, 16.0F }, 0)),
                        ModelLoader.defaultTextureGetter().apply(MaterialIconType.ore.getBlockPath(ore.material.getMaterialIconSet())),
                        side,
                        ModelRotation.X0_Y0,
                        null,
                        true,
                        true);
                if (isEmissiveLayer) {
                    materialFaceQuad = CustomTexture.rebake(15, 15, materialFaceQuad);
                }
                materialFace.put(side, materialFaceQuad);
            }
            quads.add(materialFaceQuad);
        } else {
            ItemStack stack = OreItemOverride.INSTANCE.stack.get();
            if (!stack.isEmpty()) {
                BlockOre ore = (BlockOre) ((ItemBlock) stack.getItem()).getBlock();
                IBlockState oreState = ore.getDefaultState().withProperty(ore.STONE_TYPE, ore.STONE_TYPE.getAllowedValues().get(stack.getMetadata()));
                for (EnumFacing face : EnumFacing.VALUES) {
                    quads.addAll(getQuads(oreState, face, rand));
                }
            }
        }
        return quads;
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
        return Pair.of(this, ModelFactory.getBlockTransform(cameraTransformType).getMatrix());
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
