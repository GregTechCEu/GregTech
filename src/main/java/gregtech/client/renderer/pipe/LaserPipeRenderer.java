package gregtech.client.renderer.pipe;

import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.laser.LaserPipeType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class LaserPipeRenderer extends PipeRenderer {
    public static final LaserPipeRenderer INSTANCE = new LaserPipeRenderer();
    private final EnumMap<LaserPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(LaserPipeType.class);
    public LaserPipeRenderer() {
        super("gt_laser_pipe", GTUtility.gregtechId("laser_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(LaserPipeType.NORMAL, Textures.LASER_PIPE_SIDE);
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (pipeType instanceof LaserPipeType) {
            renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                    .addSideRender(false, new IconTransformation(Textures.LASER_PIPE_SIDE));
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.LASER_PIPE_SIDE;
    }
}
