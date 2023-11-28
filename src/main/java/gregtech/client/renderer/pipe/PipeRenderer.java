package gregtech.client.renderer.pipe;

import gregtech.api.cover.CoverHolder;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ModCompatibility;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

import codechicken.lib.lighting.LightMatrix;
import codechicken.lib.render.BlockRenderer;
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class PipeRenderer implements ICCBlockRenderer, IItemRenderer {

    public final ModelResourceLocation modelLocation;
    private final String name;
    private EnumBlockRenderType blockRenderType;
    protected static final ThreadLocal<BlockRenderer.BlockFace> blockFaces = ThreadLocal
            .withInitial(BlockRenderer.BlockFace::new);
    private static final Cuboid6 FRAME_RENDER_CUBOID = new Cuboid6(0.001, 0.001, 0.001, 0.999, 0.999, 0.999);
    private static final EnumMap<EnumFacing, EnumMap<Border, EnumFacing>> FACE_BORDER_MAP = new EnumMap<>(
            EnumFacing.class);
    private static final Int2ObjectMap<IVertexOperation[]> RESTRICTOR_MAP = new Int2ObjectOpenHashMap<>();

    @SuppressWarnings("unused")
    public static void initializeRestrictor(TextureMap map) {
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_UP, Border.TOP);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_DOWN, Border.BOTTOM);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_UD, Border.TOP, Border.BOTTOM);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_LEFT, Border.LEFT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_UL, Border.TOP, Border.LEFT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_DL, Border.BOTTOM, Border.LEFT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_NR, Border.TOP, Border.BOTTOM, Border.LEFT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_RIGHT, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_UR, Border.TOP, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_DR, Border.BOTTOM, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_NL, Border.TOP, Border.BOTTOM, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_LR, Border.LEFT, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_ND, Border.TOP, Border.LEFT, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY_NU, Border.BOTTOM, Border.LEFT, Border.RIGHT);
        addRestrictor(Textures.PIPE_BLOCKED_OVERLAY, Border.TOP, Border.BOTTOM, Border.LEFT, Border.RIGHT);
    }

    static {
        FACE_BORDER_MAP.put(EnumFacing.DOWN,
                borderMap(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST));
        FACE_BORDER_MAP.put(EnumFacing.UP,
                borderMap(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST));
        FACE_BORDER_MAP.put(EnumFacing.NORTH,
                borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST));
        FACE_BORDER_MAP.put(EnumFacing.SOUTH,
                borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST));
        FACE_BORDER_MAP.put(EnumFacing.WEST,
                borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH));
        FACE_BORDER_MAP.put(EnumFacing.EAST,
                borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH));
    }

    public PipeRenderer(String name, ModelResourceLocation modelLocation) {
        this.name = name;
        this.modelLocation = modelLocation;
    }

    public PipeRenderer(String name, ResourceLocation modelLocation) {
        this(name, new ModelResourceLocation(modelLocation, "normal"));
    }

    public void preInit() {
        blockRenderType = BlockRenderingRegistry.createRenderType(name);
        BlockRenderingRegistry.registerRenderer(blockRenderType, this);
        MinecraftForge.EVENT_BUS.register(this);
        TextureUtils.addIconRegister(this::registerIcons);
    }

    public ModelResourceLocation getModelLocation() {
        return modelLocation;
    }

    public EnumBlockRenderType getBlockRenderType() {
        return blockRenderType;
    }

    public abstract void registerIcons(TextureMap map);

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(modelLocation, this);
    }

    public abstract void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe,
                                       @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType,
                                       @Nullable Material material);

    @Override
    public void renderItem(ItemStack rawItemStack, TransformType transformType) {
        ItemStack stack = ModCompatibility.getRealItemStack(rawItemStack);
        if (!(stack.getItem() instanceof ItemBlockPipe)) {
            return;
        }
        CCRenderState renderState = CCRenderState.instance();
        GlStateManager.enableBlend();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        BlockPipe<?, ?, ?> blockFluidPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stack.getItem()).getBlock();
        IPipeType<?> pipeType = blockFluidPipe.getItemPipeType(stack);
        Material material = blockFluidPipe instanceof BlockMaterialPipe blockMaterialPipe ?
                blockMaterialPipe.getItemMaterial(stack) : null;
        if (pipeType != null) {
            // 12 == 0b1100 is North and South connection (index 2 & 3)
            PipeRenderContext renderContext = new PipeRenderContext(12, 0, pipeType.getThickness());
            renderContext.color = GTUtility.convertRGBtoOpaqueRGBA_CL(getPipeColor(material, -1));
            buildRenderer(renderContext, blockFluidPipe, null, pipeType, material);
            renderPipeBlock(renderState, renderContext);
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

        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) state.getBlock();
        IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(world, pos);

        if (pipeTile == null) {
            return false;
        }

        IPipeType<?> pipeType = pipeTile.getPipeType();
        Material pipeMaterial = pipeTile instanceof TileEntityMaterialPipeBase ?
                ((TileEntityMaterialPipeBase<?, ?>) pipeTile).getPipeMaterial() : null;
        int paintingColor = pipeTile.getPaintingColor();
        int connectedSidesMap = pipeTile.getVisualConnections();
        int blockedConnections = pipeTile.getBlockedConnections();

        if (pipeType != null) {
            BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
            boolean[] sideMask = new boolean[EnumFacing.VALUES.length];
            for (EnumFacing side : EnumFacing.VALUES) {
                sideMask[side.getIndex()] = state.shouldSideBeRendered(world, pos, side);
            }
            Textures.RENDER_STATE.set(new CubeRendererState(renderLayer, sideMask, world));
            if (canRenderInLayer(renderLayer)) {
                renderState.lightMatrix.locate(world, pos);
                PipeRenderContext renderContext = new PipeRenderContext(pos, renderState.lightMatrix, connectedSidesMap,
                        blockedConnections, pipeType.getThickness());
                renderContext.color = GTUtility.convertRGBtoOpaqueRGBA_CL(getPipeColor(pipeMaterial, paintingColor));
                buildRenderer(renderContext, blockPipe, pipeTile, pipeType, pipeMaterial);
                if (renderLayer == BlockRenderLayer.CUTOUT) {
                    renderPipeBlock(renderState, renderContext);
                    renderFrame(pipeTile, pos, renderState, connectedSidesMap);
                } else {
                    renderOtherLayers(renderLayer, renderState, renderContext);
                }
            }

            CoverHolder coverHolder = pipeTile.getCoverableImplementation();
            coverHolder.renderCovers(renderState, new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ()),
                    renderLayer);
            Textures.RENDER_STATE.set(null);
        }
        return true;
    }

    private static void renderFrame(IPipeTile<?, ?> pipeTile, BlockPos pos, CCRenderState renderState,
                                    int connections) {
        Material frameMaterial = pipeTile.getFrameMaterial();
        if (frameMaterial != null) {
            ResourceLocation rl = MaterialIconType.frameGt.getBlockTexturePath(frameMaterial.getMaterialIconSet());
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
            IVertexOperation[] pipeline = {
                    new Translation(pos),
                    renderState.lightMatrix,
                    new IconTransformation(sprite),
                    new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB()))
            };

            for (EnumFacing side : EnumFacing.VALUES) {
                // only render frame if it doesn't have a cover
                if ((connections & 1 << (12 + side.getIndex())) == 0) {
                    BlockRenderer.BlockFace blockFace = blockFaces.get();
                    blockFace.loadCuboidFace(FRAME_RENDER_CUBOID, side.getIndex());
                    renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
                    renderState.render();
                }
            }
        }
    }

    private static int getPipeColor(Material material, int paintingColor) {
        if (paintingColor == -1) {
            return material == null ? 0xFFFFFF : material.getMaterialRGB();
        }
        return paintingColor;
    }

    public void renderPipeBlock(CCRenderState renderState, PipeRenderContext renderContext) {
        Cuboid6 cuboid6 = BlockPipe.getSideBox(null, renderContext.pipeThickness);
        if ((renderContext.connections & 63) == 0) {
            // base pipe without connections
            for (EnumFacing renderedSide : EnumFacing.VALUES) {
                renderOpenFace(renderState, renderContext, renderedSide, cuboid6);
            }
        } else {
            for (EnumFacing renderedSide : EnumFacing.VALUES) {
                // if connection is blocked
                if ((renderContext.connections & 1 << renderedSide.getIndex()) == 0) {
                    int oppositeIndex = renderedSide.getOpposite().getIndex();
                    if ((renderContext.connections & 1 << oppositeIndex) > 0 &&
                            (renderContext.connections & 63 & ~(1 << oppositeIndex)) == 0) {
                        // render open texture if opposite is open and no other
                        renderOpenFace(renderState, renderContext, renderedSide, cuboid6);
                    } else {
                        // else render pipe side
                        renderPipeSide(renderState, renderContext, renderedSide, cuboid6);
                    }
                } else {
                    // else render connection cuboid
                    renderPipeCube(renderState, renderContext, renderedSide);
                }
            }
        }
    }

    protected void renderPipeCube(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side) {
        Cuboid6 cuboid = BlockPipe.getSideBox(side, renderContext.pipeThickness);
        boolean doRenderBlockedOverlay = (renderContext.blockedConnections & (1 << side.getIndex())) > 0;
        // render connection cuboid
        for (EnumFacing renderedSide : EnumFacing.VALUES) {
            if (renderedSide.getAxis() != side.getAxis()) {
                // render side textures
                renderPipeSide(renderState, renderContext, renderedSide, cuboid);
                if (doRenderBlockedOverlay) {
                    // render blocked connections
                    renderFace(renderState, renderContext.blockedOverlay, renderedSide, cuboid);
                }
            }
        }
        if ((renderContext.connections & 1 << (6 + side.getIndex())) > 0) {
            // if neighbour pipe is smaller, render closed texture
            renderPipeSide(renderState, renderContext, side, cuboid);
        } else {
            if ((renderContext.connections & 1 << (12 + side.getIndex())) > 0) {
                // if face has a cover offset face by 0.001 to avoid z fighting
                cuboid = BlockPipe.getCoverSideBox(side, renderContext.pipeThickness);
            }
            renderOpenFace(renderState, renderContext, side, cuboid);
        }
    }

    protected void renderOpenFace(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side,
                                  Cuboid6 cuboid6) {
        for (IVertexOperation[] vertexOperations : renderContext.openFaceRenderer) {
            renderFace(renderState, vertexOperations, side, cuboid6);
        }
    }

    protected void renderPipeSide(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side,
                                  Cuboid6 cuboid6) {
        for (IVertexOperation[] vertexOperations : renderContext.pipeSideRenderer) {
            renderFace(renderState, vertexOperations, side, cuboid6);
        }
        int blockedConnections = renderContext.getBlockedConnections();
        int connections = renderContext.getConnections();
        if (blockedConnections != 0) {
            int borderMask = 0;
            for (Border border : Border.VALUES) {
                EnumFacing borderSide = getSideAtBorder(side, border);
                if (TileEntityPipeBase.isFaceBlocked(blockedConnections, borderSide) &&
                        TileEntityPipeBase.isConnected(connections, borderSide)) {
                    // only render when the side is blocked *and* connected
                    borderMask |= border.mask;
                }
            }
            if (borderMask != 0) {
                IVertexOperation[] pipeline = ArrayUtils.addAll(renderContext.getBaseVertexOperation(),
                        RESTRICTOR_MAP.get(borderMask));
                renderFace(renderState, pipeline, side, cuboid6);
            }
        }
    }

    protected void renderFace(CCRenderState renderState, IVertexOperation[] pipeline, EnumFacing side,
                              Cuboid6 cuboid6) {
        BlockRenderer.BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(cuboid6, side.getIndex());
        renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
        renderState.render();
    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {}

    /**
     * Override to render in other layers, e.g. emissive stuff
     * {@link #canRenderInLayer} also need to be overridden
     */
    protected void renderOtherLayers(BlockRenderLayer layer, CCRenderState renderState,
                                     PipeRenderContext renderContext) {}

    /**
     * What layers can be rendered in.
     * See also {@link #renderOtherLayers}
     * 
     * @param layer the current layer being rendered too
     * @return true if this should render in {@code layer}
     */
    protected boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite,
                                        BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) state.getBlock();
        IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return;
        }
        IPipeType<?> pipeType = pipeTile.getPipeType();
        if (pipeType == null) {
            return;
        }
        float thickness = pipeType.getThickness();
        int connectedSidesMask = pipeTile.getConnections();
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
    public void registerTextures(TextureMap map) {}

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

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<?, ?> pipeTile) {
        if (pipeTile == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        IPipeType<?> pipeType = pipeTile.getPipeType();
        Material material = pipeTile instanceof TileEntityMaterialPipeBase ?
                ((TileEntityMaterialPipeBase<?, ?>) pipeTile).getPipeMaterial() : null;
        if (pipeType == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        TextureAtlasSprite atlasSprite = getParticleTexture(pipeType, material);
        int pipeColor = getPipeColor(material, pipeTile.getPaintingColor());
        return Pair.of(atlasSprite, pipeColor);
    }

    public abstract TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material);

    public static class PipeRenderContext {

        private final BlockPos pos;
        private final LightMatrix lightMatrix;
        protected final List<IVertexOperation[]> openFaceRenderer = new ArrayList<>();
        protected final List<IVertexOperation[]> pipeSideRenderer = new ArrayList<>();
        // Blocked overlay is used for the pipe connector cube, not the main cube
        private final IVertexOperation[] blockedOverlay;
        private final float pipeThickness;
        private int color;
        private final int connections;
        private final int blockedConnections;

        public PipeRenderContext(BlockPos pos, LightMatrix lightMatrix, int connections, int blockedConnections,
                                 float thickness) {
            this.pos = pos;
            this.lightMatrix = lightMatrix;
            this.connections = connections;
            this.blockedConnections = blockedConnections;
            this.pipeThickness = thickness;
            if (pos != null && lightMatrix != null) {
                blockedOverlay = new IVertexOperation[] { new Translation(pos), lightMatrix,
                        new IconTransformation(Textures.PIPE_BLOCKED_OVERLAY) };
            } else {
                blockedOverlay = new IVertexOperation[] { new IconTransformation(Textures.PIPE_BLOCKED_OVERLAY) };
            }
        }

        public PipeRenderContext(int connections, int blockedConnections, float thickness) {
            this(null, null, connections, blockedConnections, thickness);
        }

        public PipeRenderContext addOpenFaceRender(IVertexOperation... vertexOperations) {
            return addOpenFaceRender(true, vertexOperations);
        }

        public PipeRenderContext addOpenFaceRender(boolean applyDefaultColor, IVertexOperation... vertexOperations) {
            IVertexOperation[] baseVertexOperation = getBaseVertexOperation();
            baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, vertexOperations);
            if (applyDefaultColor) {
                baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, getColorOperation());
            }
            openFaceRenderer.add(baseVertexOperation);
            return this;
        }

        public PipeRenderContext addSideRender(IVertexOperation... vertexOperations) {
            return addSideRender(true, vertexOperations);
        }

        public PipeRenderContext addSideRender(boolean applyDefaultColor, IVertexOperation... vertexOperations) {
            IVertexOperation[] baseVertexOperation = getBaseVertexOperation();
            baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, vertexOperations);
            if (applyDefaultColor) {
                baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, getColorOperation());
            }
            pipeSideRenderer.add(baseVertexOperation);
            return this;
        }

        public ColourMultiplier getColorOperation() {
            return new ColourMultiplier(color);
        }

        protected IVertexOperation[] getBaseVertexOperation() {
            if (pos == null) {
                return lightMatrix == null ? new IVertexOperation[0] : new IVertexOperation[] { lightMatrix };
            }
            return lightMatrix == null ? new IVertexOperation[] { new Translation(pos) } :
                    new IVertexOperation[] { new Translation(pos), lightMatrix };
        }

        public int getConnections() {
            return connections;
        }

        public int getBlockedConnections() {
            return blockedConnections;
        }

        public List<IVertexOperation[]> getPipeSideRenderer() {
            return pipeSideRenderer;
        }

        public List<IVertexOperation[]> getOpenFaceRenderer() {
            return openFaceRenderer;
        }

        public float getPipeThickness() {
            return pipeThickness;
        }
    }

    private static EnumMap<Border, EnumFacing> borderMap(EnumFacing topSide, EnumFacing bottomSide, EnumFacing leftSide,
                                                         EnumFacing rightSide) {
        EnumMap<Border, EnumFacing> sideMap = new EnumMap<>(Border.class);
        sideMap.put(Border.TOP, topSide);
        sideMap.put(Border.BOTTOM, bottomSide);
        sideMap.put(Border.LEFT, leftSide);
        sideMap.put(Border.RIGHT, rightSide);
        return sideMap;
    }

    private static void addRestrictor(TextureAtlasSprite sprite, Border... borders) {
        int mask = 0;
        for (Border border : borders) {
            mask |= border.mask;
        }
        RESTRICTOR_MAP.put(mask, new IVertexOperation[] { new IconTransformation(sprite) });
    }

    protected static EnumFacing getSideAtBorder(EnumFacing side, Border border) {
        return FACE_BORDER_MAP.get(side).get(border);
    }

    public enum Border {

        TOP,
        BOTTOM,
        LEFT,
        RIGHT;

        public static final Border[] VALUES = values();

        public final int mask;

        Border() {
            mask = 1 << this.ordinal();
        }
    }
}
