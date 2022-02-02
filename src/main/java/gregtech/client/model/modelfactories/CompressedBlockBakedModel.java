package gregtech.client.model.modelfactories;

import gregtech.client.model.ModelFactory;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.common.blocks.BlockCompressed;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class CompressedBlockBakedModel implements IBakedModel {

    private final Map<MaterialIconSet, Map<EnumFacing, BakedQuad>> materialFaces;
    private final ThreadLocal<TextureAtlasSprite> particle;

    public CompressedBlockBakedModel() {
        this.materialFaces = new Object2ObjectOpenHashMap<>();
        this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();
        if (side == null) return quads;
        if (state != null) {
            Material material = state.getValue(((BlockCompressed) state.getBlock()).variantProperty);
            Map<EnumFacing, BakedQuad> materialFace = materialFaces.get(material.getMaterialIconSet());
            if (materialFace == null) {
                materialFaces.put(material.getMaterialIconSet(), materialFace = new Object2ObjectOpenHashMap<>());
            }
            BakedQuad materialFaceQuad = materialFace.get(side);
            if (materialFaceQuad == null) {
                materialFace.put(side, materialFaceQuad = ModelFactory.getBakery().makeBakedQuad(
                        new Vector3f(0F, 0F, 0F),
                        new Vector3f(16F, 16F, 16F),
                        new BlockPartFace(side, 1, "", new BlockFaceUV(new float[] { 0.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.0F, 16.0F, 16.0F }, 0)),
                        ModelLoader.defaultTextureGetter().apply(MaterialIconType.block.getBlockPath(material.getMaterialIconSet())),
                        side,
                        ModelRotation.X0_Y0,
                        null,
                        true,
                        true));
            }
            quads.add(materialFaceQuad);
            particle.set(materialFaceQuad.getSprite());
        } else {
            ItemStack stack = CompressedBlockItemOverride.INSTANCE.stack.get();
            if (!stack.isEmpty()) {
                BlockCompressed compressed = (BlockCompressed) ((ItemBlock) stack.getItem()).getBlock();
                IBlockState compressedState = compressed.getDefaultState().withProperty(compressed.variantProperty, compressed.variantProperty.getAllowedValues().get(stack.getMetadata()));
                for (EnumFacing face : EnumFacing.VALUES) {
                    quads.addAll(getQuads(compressedState, face, rand));
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
        TRSRTransformation blockTransform = ModelFactory.getBlockTransform(cameraTransformType);
        return blockTransform == null ? IBakedModel.super.handlePerspective(cameraTransformType) : Pair.of(this, blockTransform.getMatrix());
    }

    @Override
    public ItemOverrideList getOverrides() {
        return CompressedBlockItemOverride.INSTANCE;
    }

    private static class CompressedBlockItemOverride extends ItemOverrideList {

        private static final CompressedBlockItemOverride INSTANCE = new CompressedBlockItemOverride();

        private final ThreadLocal<ItemStack> stack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            this.stack.set(stack);
            return originalModel;
        }

    }

}
