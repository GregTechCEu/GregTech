package gregtech.client.renderer.pipe;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.BlockRenderer.BlockFace;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.pipeline.ColourMultiplier;
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
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ModCompatibility;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class CableRenderer implements ICCBlockRenderer, IItemRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "cable"), "normal");
    public static final CableRenderer INSTANCE = new CableRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;
    private static final ThreadLocal<BlockFace> blockFaces = ThreadLocal.withInitial(BlockFace::new);

    private final TextureAtlasSprite[] insulationTextures = new TextureAtlasSprite[6];
    private TextureAtlasSprite wireTexture;

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gt_cable");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(INSTANCE::registerIcons);
    }

    public void registerIcons(TextureMap map) {
        GTLog.logger.info("Registering cable textures.");
        ResourceLocation wireLocation = new ResourceLocation(GTValues.MODID, "blocks/cable/wire");
        this.wireTexture = map.registerSprite(wireLocation);
        for (int i = 0; i < insulationTextures.length; i++) {
            ResourceLocation location = new ResourceLocation(GTValues.MODID, "blocks/cable/insulation_" + i);
            this.insulationTextures[i] = map.registerSprite(location);
        }
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        GTLog.logger.info("Injected cable render model");
        event.getModelRegistry().putObject(MODEL_LOCATION, this);
    }

    @Override
    public void renderItem(ItemStack rawItemStack, TransformType transformType) {
        ItemStack stack = ModCompatibility.getRealItemStack(rawItemStack);
        if (!(stack.getItem() instanceof ItemBlockCable)) {
            return;
        }
        GlStateManager.enableBlend();
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        BlockCable blockCable = (BlockCable) ((ItemBlockCable) stack.getItem()).getBlock();
        Insulation insulation = blockCable.getItemPipeType(stack);
        Material material = blockCable.getItemMaterial(stack);
        if (insulation != null && material != null) {
            int connections = 1 << EnumFacing.SOUTH.getIndex() | 1 << EnumFacing.NORTH.getIndex();
            renderCableBlock(material, insulation, 0x404040, renderState, new IVertexOperation[0], connections);
        }
        renderState.draw();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setBrightness(world, pos);

        BlockCable blockCable = (BlockCable) state.getBlock();
        TileEntityCable tileEntityCable = (TileEntityCable) blockCable.getPipeTileEntity(world, pos);
        if (tileEntityCable == null) return false;
        int paintingColor = tileEntityCable.getInsulationColor();
        int connectedSidesMask = blockCable.getVisualConnections(tileEntityCable);
        Insulation insulation = tileEntityCable.getPipeType();
        Material material = tileEntityCable.getPipeMaterial();
        if (insulation != null && material != null) {
            BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
            boolean[] sideMask = new boolean[EnumFacing.VALUES.length];
            for (EnumFacing side : EnumFacing.VALUES) {
                sideMask[side.getIndex()] = state.shouldSideBeRendered(world, pos, side);
            }
            Textures.RENDER_STATE.set(new CubeRendererState(renderLayer, sideMask, world));
            if (renderLayer == BlockRenderLayer.CUTOUT) {
                renderState.lightMatrix.locate(world, pos);
                IVertexOperation[] pipeline = new IVertexOperation[]{new Translation(pos), renderState.lightMatrix};
                renderCableBlock(material, insulation, paintingColor, renderState, pipeline, connectedSidesMask);
            }

            ICoverable coverable = tileEntityCable.getCoverableImplementation();
            coverable.renderCovers(renderState, new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ()), renderLayer);
            Textures.RENDER_STATE.set(null);
        }
        return true;
    }

    public void renderCableBlock(Material material, Insulation insulation1, int insulationColor1, CCRenderState state, IVertexOperation[] pipeline, int connectMask) {
        int wireColor = GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB());
        float thickness = insulation1.getThickness();

        IVertexOperation[] wire = ArrayUtils.addAll(pipeline, new IconTransformation(wireTexture), new ColourMultiplier(wireColor));
        IVertexOperation[] overlays = wire;
        IVertexOperation[] insulation = wire;

        if (insulation1.insulationLevel != -1) {
            int insulationColor = GTUtility.convertRGBtoOpaqueRGBA_CL(insulationColor1);
            ColourMultiplier multiplier = new ColourMultiplier(insulationColor);
            insulation = ArrayUtils.addAll(pipeline, new IconTransformation(insulationTextures[5]), multiplier);
            overlays = ArrayUtils.addAll(pipeline, new IconTransformation(insulationTextures[insulation1.insulationLevel]), multiplier);
        }

        Cuboid6 cuboid6 = BlockCable.getSideBox(null, thickness);
        for (EnumFacing renderedSide : EnumFacing.VALUES) {
            if ((connectMask & 1 << renderedSide.getIndex()) == 0) {
                int oppositeIndex = renderedSide.getOpposite().getIndex();
                if ((connectMask & 1 << oppositeIndex) > 0 && (connectMask & 63 & ~(1 << oppositeIndex)) == 0) {
                    //if there is something on opposite side, render overlay + wire
                    renderCableSide(state, wire, renderedSide, cuboid6);
                    renderCableSide(state, overlays, renderedSide, cuboid6);
                } else {
                    renderCableSide(state, insulation, renderedSide, cuboid6);
                }
            } else {

                renderCableCube(connectMask, state, insulation, wire, overlays, renderedSide, thickness);
            }
        }
    }

    private static void renderCableCube(int connections, CCRenderState renderState, IVertexOperation[] pipeline, IVertexOperation[] wire, IVertexOperation[] overlays, EnumFacing side, float thickness) {
        Cuboid6 cuboid = BlockCable.getSideBox(side, thickness);
        for (EnumFacing renderedSide : EnumFacing.VALUES) {
            if (renderedSide.getAxis() != side.getAxis()) {
                renderCableSide(renderState, pipeline, renderedSide, cuboid);
            }
        }
        if ((connections & 1 << (6 + side.getIndex())) > 0) {
            renderCableSide(renderState, pipeline, side, cuboid);
        } else {
            if((connections & 1 << (12 + side.getIndex())) > 0)
                cuboid = BlockCable.getCoverSideBox(side, thickness);
            renderCableSide(renderState, wire, side, cuboid);
            renderCableSide(renderState, overlays, side, cuboid);
        }
    }

    private static void renderCableSide(CCRenderState renderState, IVertexOperation[] pipeline, EnumFacing side, Cuboid6 cuboid6) {
        BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(cuboid6, side.getIndex());
        renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
        renderState.render();
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
        BlockCable blockCable = (BlockCable) state.getBlock();
        IPipeTile<Insulation, WireProperties> tileEntityCable = blockCable.getPipeTileEntity(world, pos);
        if (tileEntityCable == null) {
            return;
        }
        Insulation insulation = tileEntityCable.getPipeType();
        if (insulation == null) {
            return;
        }
        float thickness = insulation.getThickness();
        int connectedSidesMask = blockCable.getVisualConnections(tileEntityCable);
        Cuboid6 baseBox = BlockCable.getSideBox(null, thickness);
        BlockRenderer.renderCuboid(renderState, baseBox, 0);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            if ((connectedSidesMask & (1 << renderSide.getIndex())) > 0) {
                Cuboid6 sideBox = BlockCable.getSideBox(renderSide, thickness);
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

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<Insulation, WireProperties> tileEntity) {
        if (tileEntity == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        Material material = ((TileEntityCable) tileEntity).getPipeMaterial();
        Insulation insulation = tileEntity.getPipeType();
        if (material == null || insulation == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        TextureAtlasSprite atlasSprite;
        int particleColor;
        if (insulation.insulationLevel == -1) {
            atlasSprite = wireTexture;
            particleColor = material.getMaterialRGB();
        } else {
            atlasSprite = insulationTextures[5];
            particleColor = tileEntity.getInsulationColor();
        }
        return Pair.of(atlasSprite, particleColor);
    }
}
