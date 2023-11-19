package gregtech.client.renderer.pipe;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class FluidPipeRenderer extends PipeRenderer {

    public static final FluidPipeRenderer INSTANCE = new FluidPipeRenderer();
    private final EnumMap<FluidPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(FluidPipeType.class);
    private final EnumMap<FluidPipeType, TextureAtlasSprite> pipeTexturesWood = new EnumMap<>(FluidPipeType.class);
    private final EnumMap<EnumFacing, EnumMap<Border, EnumFacing>> faceBorderMap = new EnumMap<>(EnumFacing.class);
    private final Int2ObjectMap<IVertexOperation[]> restrictorMap = new Int2ObjectOpenHashMap<>();

    private FluidPipeRenderer() {
        super("gt_fluid_pipe", GTUtility.gregtechId("fluid_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(FluidPipeType.TINY, Textures.PIPE_TINY);
        pipeTextures.put(FluidPipeType.SMALL, Textures.PIPE_SMALL);
        pipeTextures.put(FluidPipeType.NORMAL, Textures.PIPE_NORMAL);
        pipeTextures.put(FluidPipeType.LARGE, Textures.PIPE_LARGE);
        pipeTextures.put(FluidPipeType.HUGE, Textures.PIPE_HUGE);
        pipeTextures.put(FluidPipeType.QUADRUPLE, Textures.PIPE_QUADRUPLE);
        pipeTextures.put(FluidPipeType.NONUPLE, Textures.PIPE_NONUPLE);

        pipeTexturesWood.put(FluidPipeType.SMALL, Textures.PIPE_SMALL_WOOD);
        pipeTexturesWood.put(FluidPipeType.NORMAL, Textures.PIPE_NORMAL_WOOD);
        pipeTexturesWood.put(FluidPipeType.LARGE, Textures.PIPE_LARGE_WOOD);

        faceBorderMap.put(EnumFacing.DOWN, borderMap(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST));
        faceBorderMap.put(EnumFacing.UP, borderMap(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST));
        faceBorderMap.put(EnumFacing.NORTH, borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST));
        faceBorderMap.put(EnumFacing.SOUTH, borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST));
        faceBorderMap.put(EnumFacing.WEST, borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH));
        faceBorderMap.put(EnumFacing.EAST, borderMap(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH));

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

    private static EnumMap<Border, EnumFacing> borderMap(EnumFacing topSide, EnumFacing bottomSide, EnumFacing leftSide, EnumFacing rightSide) {
        EnumMap<Border, EnumFacing> sideMap = new EnumMap<>(Border.class);
        sideMap.put(Border.TOP, topSide);
        sideMap.put(Border.BOTTOM, bottomSide);
        sideMap.put(Border.LEFT, leftSide);
        sideMap.put(Border.RIGHT, rightSide);
        return sideMap;
    }

    private void addRestrictor(TextureAtlasSprite sprite, Border... borders) {
        int mask = 0;
        for (Border border : borders) {
            mask |= border.mask;
        }
        restrictorMap.put(mask, new IVertexOperation[]{new IconTransformation(sprite)});
    }

    protected EnumFacing getSideAtBorder(EnumFacing side, Border border) {
        return faceBorderMap.get(side).get(border);
    }

    public enum Border {
        TOP, BOTTOM, LEFT, RIGHT;

        public static final Border[] VALUES = values();

        public final int mask;

        Border() {
            mask = 1 << this.ordinal();
        }
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof FluidPipeType)) {
            return;
        }
        if (ModHandler.isMaterialWood(material)) {
            TextureAtlasSprite sprite = pipeTexturesWood.get(pipeType);
            if(sprite != null) {
                renderContext.addOpenFaceRender(new IconTransformation(sprite));
            } else {
                renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)));
            }
            renderContext.addSideRender(new IconTransformation(Textures.PIPE_SIDE_WOOD));
        } else {
            renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                    .addSideRender(new IconTransformation(Textures.PIPE_SIDE));
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.PIPE_SIDE;
    }

    @Override
    protected void renderPipeSide(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side, Cuboid6 cuboid6) {
        for (IVertexOperation[] vertexOperations : renderContext.pipeSideRenderer) {
            renderFace(renderState, vertexOperations, side, cuboid6);
        }
        int blockedConnections = renderContext.getBlockedConnections();
        if (blockedConnections != 0) {
            int borderMask = 0;
            for (Border border : Border.VALUES) {
                if (TileEntityPipeBase.isFaceBlocked(blockedConnections, getSideAtBorder(side, border))) {
                    borderMask |= border.mask;
                }
            }
            if (borderMask != 0) {
                IVertexOperation[] pipeline = ArrayUtils.addAll(renderContext.getBaseVertexOperation(), restrictorMap.get(borderMask));
                renderFace(renderState, pipeline, side, cuboid6);
            }
        }
    }
}
