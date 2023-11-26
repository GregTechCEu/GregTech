package gregtech.client.renderer.pipe;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.laser.LaserPipeType;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.uv.IconTransformation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class LaserPipeRenderer extends PipeRenderer {

    public static final LaserPipeRenderer INSTANCE = new LaserPipeRenderer();
    private final EnumMap<LaserPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(LaserPipeType.class);
    private boolean active = false;

    public LaserPipeRenderer() {
        super("gt_laser_pipe", GTUtility.gregtechId("laser_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(LaserPipeType.NORMAL, Textures.LASER_PIPE_IN);
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe,
                              @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (pipeType instanceof LaserPipeType) {
            renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                    .addSideRender(false, new IconTransformation(Textures.LASER_PIPE_SIDE));
            if (pipeTile != null && pipeTile.isPainted()) {
                renderContext.addSideRender(new IconTransformation(Textures.LASER_PIPE_OVERLAY));
            }

            active = !ConfigHolder.client.preventAnimatedCables && pipeTile instanceof TileEntityLaserPipe laserPipe &&
                    laserPipe.isActive();
        }
    }

    @Override
    protected void renderOtherLayers(BlockRenderLayer layer, CCRenderState renderState,
                                     PipeRenderContext renderContext) {
        if (active && layer == BloomEffectUtil.getEffectiveBloomLayer() &&
                (renderContext.getConnections() & 0b111111) != 0) {
            Cuboid6 innerCuboid = BlockPipe.getSideBox(null, renderContext.getPipeThickness());
            if ((renderContext.getConnections() & 0b111111) != 0) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    if ((renderContext.getConnections() & (1 << side.getIndex())) == 0) {
                        int oppositeIndex = side.getOpposite().getIndex();
                        if ((renderContext.getConnections() & (1 << oppositeIndex)) <= 0 ||
                                (renderContext.getConnections() & 0b111111 & ~(1 << oppositeIndex)) != 0) {
                            // render pipe side
                            IVertexOperation[] ops = renderContext.getBaseVertexOperation();
                            ops = ArrayUtils.addAll(ops, new IconTransformation(Textures.LASER_PIPE_OVERLAY_EMISSIVE));
                            renderFace(renderState, ops, side, innerCuboid);
                        }
                    } else {
                        // render connection cuboid
                        Cuboid6 sideCuboid = BlockPipe.getSideBox(side, renderContext.getPipeThickness());
                        for (EnumFacing connectionSide : EnumFacing.VALUES) {
                            if (connectionSide.getAxis() != side.getAxis()) {
                                // render side textures
                                IVertexOperation[] ops = renderContext.getBaseVertexOperation();
                                ops = ArrayUtils.addAll(ops,
                                        new IconTransformation(Textures.LASER_PIPE_OVERLAY_EMISSIVE));
                                renderFace(renderState, ops, connectionSide, sideCuboid);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean canRenderInLayer(BlockRenderLayer layer) {
        return super.canRenderInLayer(layer) || layer == BloomEffectUtil.getEffectiveBloomLayer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.LASER_PIPE_SIDE;
    }
}
