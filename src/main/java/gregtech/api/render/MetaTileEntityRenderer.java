package gregtech.api.render;

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
import gregtech.api.GTValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.gui.resources.ResourceHelper;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.IRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.shader.postprocessing.IPostCCLRender;
import gregtech.api.util.GTLog;
import gregtech.api.util.ModCompatibility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SideOnly(Side.CLIENT)
public class MetaTileEntityRenderer implements ICCBlockRenderer, IItemRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "machine"), "normal");
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
        if (!(stack.getItem() instanceof MachineItemBlock)) {
            return;
        }
        MetaTileEntity metaTileEntity = MachineItemBlock.getMetaTileEntity(stack);
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
        if (metaTileEntity instanceof IRenderMetaTileEntity) {
            ((IRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityDynamic(0.0, 0.0, 0.0, 0.0f);
        }
        GlStateManager.disableBlend();
    }

    private static MetaTileEntity RENDER_MTE;

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
        if (metaTileEntity == null) {
            return false;
        }

        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        Matrix4 translation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        if (metaTileEntity.canRenderInLayer(renderLayer)) {
            RENDER_MTE = metaTileEntity;
            BLOOM_MTE.getOrDefault(RENDER_MTE, Collections.EMPTY_LIST).clear();
            renderState.lightMatrix.locate(world, pos);
            IVertexOperation[] pipeline = new IVertexOperation[]{renderState.lightMatrix};
            metaTileEntity.renderMetaTileEntity(renderState, translation.copy(), pipeline);
            RENDER_MTE = null;
        }
        Matrix4 coverTranslation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        metaTileEntity.renderCovers(renderState, coverTranslation, renderLayer);

        if (metaTileEntity.isFragile() && renderLayer == BlockRenderLayer.CUTOUT) {
            TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
            Random posRand = new Random(MathHelper.getPositionRandom(pos));
            int destroyStage = posRand.nextInt(10);
            TextureAtlasSprite atlasSprite = textureMap.getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);
            for (EnumFacing face : EnumFacing.VALUES) {
                Textures.renderFace(renderState, translation, new IVertexOperation[0], face, Cuboid6.full, atlasSprite);
            }
        }
        return true;
    }

    private static final Map<MetaTileEntity, List<IPostCCLRender>> BLOOM_MTE = new HashMap<>();

    public static void addCCLBloomPipeline(IPostCCLRender renderer) {
        if (RENDER_MTE != null && renderer != null) {
            if (!BLOOM_MTE.containsKey(RENDER_MTE)) {
                BLOOM_MTE.put(RENDER_MTE, new LinkedList<>());
            }
            BLOOM_MTE.get(RENDER_MTE).add(renderer);
        }
    }

    public static void renderBloom() {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        ResourceHelper.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);

        Iterator<Map.Entry<MetaTileEntity, List<IPostCCLRender>>> iter = BLOOM_MTE.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<MetaTileEntity, List<IPostCCLRender>> entry = iter.next();
            if (entry.getKey().isValid()) {
                renderState.lightMatrix.locate(entry.getKey().getWorld(), entry.getKey().getPos());
                for (IPostCCLRender render : entry.getValue()) {
                    render.render(renderState);
                }
            } else {
                iter.remove();
            }
        }

        renderState.draw();
        RenderHelper.enableStandardItemLighting();
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
    public void renderBrightness(IBlockState state, float brightness) {
    }

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite, BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
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

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IBlockAccess world, BlockPos pos) {
        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(world, pos);
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
    public void registerTextures(TextureMap map) {
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

}
