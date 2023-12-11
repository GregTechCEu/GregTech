package gregtech.client.renderer.pipe;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.optical.OpticalPipeType;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import codechicken.lib.vec.uv.IconTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public final class OpticalPipeRenderer extends PipeRenderer {

    public static final OpticalPipeRenderer INSTANCE = new OpticalPipeRenderer();
    private final EnumMap<OpticalPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(OpticalPipeType.class);

    private OpticalPipeRenderer() {
        super("gt_optical_pipe", GTUtility.gregtechId("optical_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(OpticalPipeType.NORMAL, Textures.OPTICAL_PIPE_IN);
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe,
                              @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (pipeType instanceof OpticalPipeType) {
            renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                    .addSideRender(false, new IconTransformation(Textures.OPTICAL_PIPE_SIDE));

            if (ConfigHolder.client.preventAnimatedCables) {
                renderContext.addSideRender(new IconTransformation(Textures.OPTICAL_PIPE_SIDE_OVERLAY));
            } else if (pipeTile instanceof TileEntityOpticalPipe opticalPipe && opticalPipe.isActive()) {
                renderContext.addSideRender(new IconTransformation(Textures.OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE));
            } else {
                renderContext.addSideRender(new IconTransformation(Textures.OPTICAL_PIPE_SIDE_OVERLAY));
            }
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.OPTICAL_PIPE_SIDE;
    }
}
