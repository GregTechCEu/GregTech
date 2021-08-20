package gregtech.common.blocks.modelfactories;

import codechicken.lib.render.item.CCRenderItem;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import gregtech.api.model.ModelFactory;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelFluid;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class BakedModelHandler {

    private static final StateMapperBase SIMPLE_STATE_MAPPER = new StateMapperBase() {
        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return getSimpleModelLocation(state.getBlock());
        }
    };
    private static final ItemMeshDefinition SIMPLE_MESH_DEFINITION = (stack) ->
            getSimpleModelLocation(Block.getBlockFromItem(stack.getItem()));

    private static ModelResourceLocation getSimpleModelLocation(Block block) {
        return new ModelResourceLocation(Block.REGISTRY.getNameForObject(block), "");
    }

    public static final EnumMap<ItemCameraTransforms.TransformType, Matrix4f> TRANSFORM_MAP_ITEM = new EnumMap<>(ItemCameraTransforms.TransformType.class);
    public static final EnumMap<ItemCameraTransforms.TransformType, Matrix4f> TRANSFORM_MAP_BLOCK = new EnumMap<>(ItemCameraTransforms.TransformType.class);

    static {
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.GUI, getTransform(0, 0, 0, 0, 0, 0, 1f).getMatrix());
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.5f).getMatrix());
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f).getMatrix());
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(0, 3, 1, 0, 0, 0, 0.55f).getMatrix());
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, getTransform(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f).getMatrix());
        TRANSFORM_MAP_ITEM.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, getTransform(0f, 4.0f, 0.5f, 0, 90, -55, 0.85f).getMatrix());

        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.GUI, getTransform(0, 0, 0, 30, 225, 0, 0.625f).getMatrix());
        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.GROUND, getTransform(0, 2, 0, 0, 0, 0, 0.25f).getMatrix());
        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 45, 0, 0.4f).getMatrix());
        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 0, 0, 0.4f).getMatrix());
        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, getTransform(0, 0, 0, 45, 0, 0, 0.4f).getMatrix());
        TRANSFORM_MAP_BLOCK.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, getTransform(0, 0, 0, 45, 0, 0, 0.4f).getMatrix());
    }

    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(new Vector3f(tx / 16, ty / 16, tz / 16), TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)), new Vector3f(s, s, s), null);
    }

    private final List<Tuple<Block, String>> builtInBlocks = new ArrayList<>();
    private final List<BlockFluidBase> fluidBlocks = new ArrayList<>();

    public void addBuiltInBlock(Block block, String particleTexture) {
        this.builtInBlocks.add(new Tuple<>(block, particleTexture));
        ModelLoader.setCustomStateMapper(block, SIMPLE_STATE_MAPPER);
        Item itemFromBlock = Item.getItemFromBlock(block);
        if (itemFromBlock != Items.AIR) {
            ModelLoader.setCustomMeshDefinition(itemFromBlock, SIMPLE_MESH_DEFINITION);
        }
    }

    public void addFluidBlock(BlockFluidBase fluidBase) {
        this.fluidBlocks.add(fluidBase);
        ModelLoader.setCustomStateMapper(fluidBase, SIMPLE_STATE_MAPPER);
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        for (BlockFluidBase fluidBlock : fluidBlocks) {
            Fluid fluid = ObfuscationReflectionHelper.getPrivateValue(BlockFluidBase.class, fluidBlock, "definedFluid");
            ModelFluid modelFluid = new ModelFluid(fluid);
            IBakedModel bakedModel = modelFluid.bake(modelFluid.getDefaultState(), DefaultVertexFormats.ITEM, TextureUtils::getTexture);
            ModelResourceLocation resourceLocation = getSimpleModelLocation(fluidBlock);
            event.getModelRegistry().putObject(resourceLocation, bakedModel);
        }
        for (Tuple<Block, String> tuple : builtInBlocks) {
            ModelResourceLocation resourceLocation = getSimpleModelLocation(tuple.getFirst());
            ModelBuiltInRenderer bakedModel = new ModelBuiltInRenderer(tuple.getSecond());
            event.getModelRegistry().putObject(resourceLocation, bakedModel);
        }
        for (BlockOre ore : MetaBlocks.ORES) {
            for (IBlockState state : ore.blockState.getValidStates()) {
                // StoneType stoneType = state.getValue(ore.STONE_TYPE);
                ModelResourceLocation loc = new ModelResourceLocation(ore.getRegistryName(), MetaBlocks.statePropertiesToString(state.getProperties()));
                /*
                IBakedModel bakedModel = new ModelFactory(ModelFactory.ModelTemplate.DOUBLE_LAYERED_BLOCK, stoneType.backgroundTopTexture)
                        .addSpriteToLayer(0, stoneType.backgroundTopTexture)
                        .addSpriteToLayer(1, MaterialIconType.ore.getBlockPath(ore.material.getMaterialIconSet()))
                        .bake();
                 */
                event.getModelRegistry().putObject(loc, OreBakedModel.INSTANCE);
            }
        }
    }

    private static class ModelBuiltInRenderer implements IBakedModel {

        private final String particleTexture;

        public ModelBuiltInRenderer(String particleTexture) {
            this.particleTexture = particleTexture;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return Collections.emptyList();
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
            return true;
        }

        @Nonnull
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return TextureUtils.getBlockTexture(particleTexture);
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        @Nonnull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull TransformType cameraTransformType) {
            CCRenderItem.notifyTransform(cameraTransformType);
            return PerspectiveMapWrapper.handlePerspective(this, TransformUtils.DEFAULT_BLOCK, cameraTransformType);
        }
    }

}

