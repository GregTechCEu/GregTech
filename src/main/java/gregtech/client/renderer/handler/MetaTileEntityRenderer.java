package gregtech.client.renderer.handler;

import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ModCompatibility;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class MetaTileEntityRenderer implements ICCBlockRenderer, IItemRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(
            GTUtility.gregtechId("machine"), "normal");
    public static final MetaTileEntityRenderer INSTANCE = new MetaTileEntityRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("meta_tile_entity");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(Textures::register);
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        GTLog.logger.info("Injected MetaTileEntity render model");
        event.getModelRegistry().putObject(MODEL_LOCATION, this);
    }

    @Override
    public void renderItem(ItemStack rawStack, TransformType transformType) {
        ItemStack stack = ModCompatibility.getRealItemStack(rawStack);
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        if (metaTileEntity == null) {
            return;
        }
        GlStateManager.enableBlend();
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        metaTileEntity.setRenderContextStack(stack);
        metaTileEntity.renderMetaTileEntity(renderState, new Matrix4(), new IVertexOperation[0]);
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState, new Matrix4(), 0.0f);
        }
        metaTileEntity.setRenderContextStack(null);
        renderState.draw();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(world, pos);
        if (metaTileEntity == null) {
            return false;
        }
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        Matrix4 translation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        boolean[] sideMask = new boolean[EnumFacing.VALUES.length];
        for (EnumFacing side : EnumFacing.VALUES) {
            sideMask[side.getIndex()] = state.shouldSideBeRendered(world, pos, side);
        }
        Textures.RENDER_STATE.set(new CubeRendererState(renderLayer, sideMask, world));
        if (metaTileEntity.canRenderInLayer(renderLayer)) {
            renderState.lightMatrix.locate(world, pos);
            IVertexOperation[] pipeline = new IVertexOperation[] { renderState.lightMatrix };
            metaTileEntity.renderMetaTileEntity(renderState, translation.copy(), pipeline);
        }

        metaTileEntity.renderCovers(renderState, translation.copy(), renderLayer);

        Textures.RENDER_STATE.set(null);
        return true;
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {}

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite,
                                        BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(world, pos);
        ArrayList<IndexedCuboid6> boundingBox = new ArrayList<>();
        if (metaTileEntity != null) {
            metaTileEntity.addCollisionBoundingBox(boundingBox);
            metaTileEntity.addCoverCollisionBoundingBox(boundingBox);
        }
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        for (Cuboid6 cuboid : boundingBox) {
            BlockRenderer.renderCuboid(renderState, cuboid, 0);
        }
    }

    public static Pair<TextureAtlasSprite, Integer> getParticleTexture(IBlockAccess world, BlockPos pos) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(world, pos);
        if (metaTileEntity == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        } else {
            return metaTileEntity.getParticleTexture();
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return TextureUtils.getMissingSprite();
    }

    @Override
    public void registerTextures(TextureMap map) {}

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }
}
