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
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ModCompatibility;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class FluidPipeRenderer implements ICCBlockRenderer, IItemRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GTValues.MODID, "fluid_pipe"), "normal");
    public static final FluidPipeRenderer INSTANCE = new FluidPipeRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;
    private static final ThreadLocal<BlockFace> blockFaces = ThreadLocal.withInitial(BlockFace::new);
    private final Map<MaterialIconSet, EnumMap<FluidPipeType, PipeTextureInfo>> pipeTextures = new HashMap<>();

    private static class PipeTextureInfo {
        public final TextureAtlasSprite inTexture;
        public final TextureAtlasSprite sideTexture;

        public PipeTextureInfo(TextureAtlasSprite inTexture, TextureAtlasSprite sideTexture) {
            this.inTexture = inTexture;
            this.sideTexture = sideTexture;
        }
    }

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gt_fluid_pipe");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(INSTANCE::registerIcons);
    }

    public void registerIcons(TextureMap map) {
        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            EnumMap<FluidPipeType, PipeTextureInfo> pipeTypeMap = new EnumMap<>(FluidPipeType.class);
            ResourceLocation sideLocation = new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + iconSet.name + "/pipe_side");
            for (FluidPipeType fluidPipeType : FluidPipeType.values()) {
                ResourceLocation inLocation = new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + iconSet.name + "/pipe_" + fluidPipeType.name + "_in");

                TextureAtlasSprite inTexture = map.registerSprite(inLocation);
                TextureAtlasSprite sideTexture = map.registerSprite(sideLocation);
                pipeTypeMap.put(fluidPipeType, new PipeTextureInfo(inTexture, sideTexture));
            }
            this.pipeTextures.put(iconSet, pipeTypeMap);
        }
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(MODEL_LOCATION, this);
    }

    @Override
    public void renderItem(ItemStack rawItemStack, TransformType transformType) {
        ItemStack stack = ModCompatibility.getRealItemStack(rawItemStack);
        if (!(stack.getItem() instanceof ItemBlockFluidPipe)) {
            return;
        }
        CCRenderState renderState = CCRenderState.instance();
        GlStateManager.enableBlend();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        BlockFluidPipe blockFluidPipe = (BlockFluidPipe) ((ItemBlockFluidPipe) stack.getItem()).getBlock();
        FluidPipeType pipeType = blockFluidPipe.getItemPipeType(stack);
        Material material = blockFluidPipe.getItemMaterial(stack);
        if (pipeType != null && material != null) {
            int connections = 1 << EnumFacing.SOUTH.getIndex() | 1 << EnumFacing.NORTH.getIndex();
            renderPipeBlock(material, pipeType, IPipeTile.DEFAULT_COVER_COLOR, renderState, new IVertexOperation[0], connections);
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

        BlockFluidPipe blockFluidPipe = ((BlockFluidPipe) state.getBlock());
        TileEntityFluidPipe tileEntityPipe = (TileEntityFluidPipe) blockFluidPipe.getPipeTileEntity(world, pos);

        if (tileEntityPipe == null) {
            return false;
        }

        FluidPipeType fluidPipeType = tileEntityPipe.getPipeType();
        Material pipeMaterial = tileEntityPipe.getPipeMaterial();
        int paintingColor = tileEntityPipe.getInsulationColor();
        int connectedSidesMap = blockFluidPipe.getVisualConnections(tileEntityPipe);

        if (fluidPipeType != null && pipeMaterial != null) {
            BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
            boolean[] sideMask = new boolean[EnumFacing.VALUES.length];
            for (EnumFacing side : EnumFacing.VALUES) {
                sideMask[side.getIndex()] = state.shouldSideBeRendered(world, pos, side);
            }
            Textures.RENDER_STATE.set(new CubeRendererState(renderLayer, sideMask, world));
            if (renderLayer == BlockRenderLayer.CUTOUT) {
                renderState.lightMatrix.locate(world, pos);
                IVertexOperation[] pipeline = new IVertexOperation[]{new Translation(pos), renderState.lightMatrix};
                renderPipeBlock(pipeMaterial, fluidPipeType, paintingColor, renderState, pipeline, connectedSidesMap);
            }

            ICoverable coverable = tileEntityPipe.getCoverableImplementation();
            coverable.renderCovers(renderState, new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ()), renderLayer);
            Textures.RENDER_STATE.set(null);
        }
        return true;
    }

    private int getPipeColor(Material material, int insulationColor) {
        if (insulationColor == IPipeTile.DEFAULT_COVER_COLOR) {
            return material.getMaterialRGB();
        } else return insulationColor;
    }

    public void renderPipeBlock(Material material, FluidPipeType pipeType, int insulationColor, CCRenderState state, IVertexOperation[] pipeline, int connectMask) {
        int pipeColor = GTUtility.convertRGBtoOpaqueRGBA_CL(getPipeColor(material, insulationColor));
        float thickness = pipeType.getThickness();
        ColourMultiplier multiplier = new ColourMultiplier(pipeColor);
        PipeTextureInfo textureInfo = this.pipeTextures.get(material.getMaterialIconSet()).get(pipeType);
        IVertexOperation[] pipeConnectSide = ArrayUtils.addAll(pipeline, new IconTransformation(textureInfo.inTexture), multiplier);
        IVertexOperation[] pipeSide = ArrayUtils.addAll(pipeline, new IconTransformation(textureInfo.sideTexture), multiplier);

        Cuboid6 cuboid6 = BlockFluidPipe.getSideBox(null, thickness);
        if (connectMask == 0) {
            for (EnumFacing renderedSide : EnumFacing.VALUES) {
                renderPipeSide(state, pipeConnectSide, renderedSide, cuboid6);
            }
        } else {
            for (EnumFacing renderedSide : EnumFacing.VALUES) {
                if ((connectMask & 1 << renderedSide.getIndex()) == 0) {
                    int oppositeIndex = renderedSide.getOpposite().getIndex();
                    if ((connectMask & 1 << oppositeIndex) > 0 && (connectMask & 63 & ~(1 << oppositeIndex)) == 0) {
                        renderPipeSide(state, pipeConnectSide, renderedSide, cuboid6);
                    } else {
                        renderPipeSide(state, pipeSide, renderedSide, cuboid6);
                    }
                } else {

                    renderPipeCube(connectMask, state, pipeSide, pipeConnectSide, renderedSide, thickness);
                }
            }
        }
    }

    private static void renderPipeCube(int connections, CCRenderState renderState, IVertexOperation[] pipeline, IVertexOperation[] pipeConnectSide, EnumFacing side, float thickness) {
        Cuboid6 cuboid = BlockFluidPipe.getSideBox(side, thickness);
        for (EnumFacing renderedSide : EnumFacing.VALUES) {
            if (renderedSide.getAxis() != side.getAxis()) {
                renderPipeSide(renderState, pipeline, renderedSide, cuboid);
            }
        }
        if ((connections & 1 << (6 + side.getIndex())) > 0) {
            // if neighbour pipe is smaller, render closed texture
            renderPipeSide(renderState, pipeline, side, cuboid);
        } else {
            if((connections & 1 << (12 + side.getIndex())) > 0)
                cuboid = BlockFluidPipe.getCoverSideBox(side, thickness);
            renderPipeSide(renderState, pipeConnectSide, side, cuboid);
        }
    }

    private static void renderPipeSide(CCRenderState renderState, IVertexOperation[] pipeline, EnumFacing side, Cuboid6 cuboid6) {
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
        BlockFluidPipe blockFluidPipe = (BlockFluidPipe) state.getBlock();
        IPipeTile<FluidPipeType, FluidPipeProperties> tileEntityPipe = blockFluidPipe.getPipeTileEntity(world, pos);
        if (tileEntityPipe == null) {
            return;
        }
        FluidPipeType fluidPipeType = tileEntityPipe.getPipeType();
        if (fluidPipeType == null) {
            return;
        }
        float thickness = fluidPipeType.getThickness();
        int connectedSidesMask = blockFluidPipe.getVisualConnections(tileEntityPipe);
        Cuboid6 baseBox = BlockFluidPipe.getSideBox(null, thickness);
        BlockRenderer.renderCuboid(renderState, baseBox, 0);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            if ((connectedSidesMask & (1 << renderSide.getIndex())) > 0) {
                Cuboid6 sideBox = BlockFluidPipe.getSideBox(renderSide, thickness);
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

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<FluidPipeType, FluidPipeProperties> tileEntity) {
        if (tileEntity == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        FluidPipeType fluidPipeType = tileEntity.getPipeType();
        Material material = ((TileEntityFluidPipe) tileEntity).getPipeMaterial();
        if (fluidPipeType == null || material == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        TextureAtlasSprite atlasSprite = pipeTextures.get(material.getMaterialIconSet()).get(fluidPipeType).sideTexture;
        int pipeColor = getPipeColor(material, tileEntity.getInsulationColor());
        return Pair.of(atlasSprite, pipeColor);
    }
}
