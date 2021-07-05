package gregtech.common.render;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.GTValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.inventory.BlockInventoryPipe;
import gregtech.common.pipelike.inventory.InventoryPipeType;
import gregtech.common.pipelike.inventory.tile.TileEntityInventoryPipe;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class InvPipeRenderer implements ICCBlockRenderer, IItemRenderer {

    public static ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "inv_pipe"), "normal");
    public static InvPipeRenderer INSTANCE = new InvPipeRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;

    private TextureAtlasSprite jointTextureSprite;
    private TextureAtlasSprite pipeTextureSprite;
    private CCModel[] fullBlockVariants;
    private CCModel[] connectionModels;

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gt_inv_pipe");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(INSTANCE::registerIcons);
    }

    public void registerIcons(TextureMap map) {
        this.jointTextureSprite = map.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/inv_pipe/joint"));
        this.pipeTextureSprite = map.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/inv_pipe/pipe"));

        InventoryPipeType pipeType = InventoryPipeType.NORMAL;
        float thickness = pipeType.getThickness();
        double height = (1.0f - thickness) / 2.0f;
        //CCModel connectionModel = ShapeModelGenerator.generateModel(3, height, thickness / 3.0f, height);
        //CCModel fullBlockModel = ShapeModelGenerator.generateModel(3, 1.0f, thickness / 3.0f, height);

        //this.fullBlockVariants = ShapeModelGenerator.generateFullBlockVariants(fullBlockModel);
        //this.connectionModels = ShapeModelGenerator.generateRotatedVariants(connectionModel);
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(MODEL_LOCATION, this);
    }

    @Override
    public void renderItem(ItemStack rawItemStack, TransformType transformType) {
        GlStateManager.enableBlend();
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        renderPipe(renderState, new IVertexOperation[0], IPipeTile.DEFAULT_INSULATION_COLOR, 12);
        renderState.draw();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setBrightness(world, pos);
        IVertexOperation[] pipeline = {new Translation(pos)};

        BlockInventoryPipe block = (BlockInventoryPipe) state.getBlock();
        TileEntityInventoryPipe tileEntity = (TileEntityInventoryPipe) block.getPipeTileEntity(world, pos);
        if (tileEntity == null) {
            return false;
        }
        int paintingColor = tileEntity.getInsulationColor();
        int connectedSidesMask = block.getActualConnections(tileEntity, world);

        BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
        if (renderLayer == BlockRenderLayer.SOLID) {
            renderPipe(renderState, pipeline, paintingColor, connectedSidesMask);
        }
        ICoverable coverable = tileEntity.getCoverableImplementation();
        coverable.renderCovers(renderState, new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ()), renderLayer);
        return true;
    }

    public void renderPipe(CCRenderState renderState, IVertexOperation[] pipeline, int insulationColor, int connectMask) {

    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {
    }

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite, BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        BlockInventoryPipe block = (BlockInventoryPipe) state.getBlock();
        TileEntityInventoryPipe tileEntity = (TileEntityInventoryPipe) block.getPipeTileEntity(world, pos);
        if (tileEntity == null) {
            return;
        }
        InventoryPipeType pipeType = tileEntity.getPipeType();
        if (pipeType == null) {
            return;
        }
        float thickness = pipeType.getThickness();
        int connectedSidesMask = block.getActualConnections(tileEntity, world);
        Cuboid6 baseBox = BlockPipe.getSideBox(null, thickness);
        BlockRenderer.renderCuboid(renderState, baseBox, 0);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            if ((connectedSidesMask & (1 << renderSide.getIndex())) > 0) {
                Cuboid6 sideBox = BlockPipe.getSideBox(renderSide, thickness);
                BlockRenderer.renderCuboid(renderState, sideBox, 0);
            }
        }
    }

    @Override
    public void registerTextures(TextureMap map) {
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return TextureUtils.getMissingSprite();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<InventoryPipeType, EmptyNodeData> tileEntity) {
        if (tileEntity == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        TextureAtlasSprite atlasSprite = TextureUtils.getBlockTexture("stone");
        int particleColor = tileEntity.getInsulationColor();
        return Pair.of(atlasSprite, particleColor);
    }
}
