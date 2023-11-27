package gregtech.client.model.modelfactories;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.item.CCRenderItem;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

@SideOnly(Side.CLIENT)
public class BakedModelHandler {

    private static final StateMapperBase SIMPLE_STATE_MAPPER = new StateMapperBase() {

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return getSimpleModelLocation(state.getBlock());
        }
    };
    private static final ItemMeshDefinition SIMPLE_MESH_DEFINITION = (stack) -> getSimpleModelLocation(
            Block.getBlockFromItem(stack.getItem()));

    private static ModelResourceLocation getSimpleModelLocation(Block block) {
        return new ModelResourceLocation(Block.REGISTRY.getNameForObject(block), "");
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
            IBakedModel bakedModel = modelFluid.bake(modelFluid.getDefaultState(), DefaultVertexFormats.ITEM,
                    TextureUtils::getTexture);
            ModelResourceLocation resourceLocation = getSimpleModelLocation(fluidBlock);
            event.getModelRegistry().putObject(resourceLocation, bakedModel);
        }
        for (Tuple<Block, String> tuple : builtInBlocks) {
            ModelResourceLocation resourceLocation = getSimpleModelLocation(tuple.getFirst());
            ModelBuiltInRenderer bakedModel = new ModelBuiltInRenderer(tuple.getSecond());
            event.getModelRegistry().putObject(resourceLocation, bakedModel);
        }
    }

    private static class ModelBuiltInRenderer implements IBakedModel {

        private final String particleTexture;

        public ModelBuiltInRenderer(String particleTexture) {
            this.particleTexture = particleTexture;
        }

        @NotNull
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

        @NotNull
        @Override
        public TextureAtlasSprite getParticleTexture() {
            return TextureUtils.getBlockTexture(particleTexture);
        }

        @NotNull
        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        @NotNull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@NotNull TransformType cameraTransformType) {
            CCRenderItem.notifyTransform(cameraTransformType);
            return PerspectiveMapWrapper.handlePerspective(this, TransformUtils.DEFAULT_BLOCK, cameraTransformType);
        }
    }
}
