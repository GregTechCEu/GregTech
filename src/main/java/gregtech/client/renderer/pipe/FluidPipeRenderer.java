package gregtech.client.renderer.pipe;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import codechicken.lib.vec.uv.IconTransformation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class FluidPipeRenderer extends PipeRenderer {

    public static final FluidPipeRenderer INSTANCE = new FluidPipeRenderer();
    private final EnumMap<FluidPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(FluidPipeType.class);
    private final EnumMap<FluidPipeType, TextureAtlasSprite> pipeTexturesWood = new EnumMap<>(FluidPipeType.class);

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
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile,
                              IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof FluidPipeType)) {
            return;
        }
        if (ModHandler.isMaterialWood(material)) {
            TextureAtlasSprite sprite = pipeTexturesWood.get(pipeType);
            if (sprite != null) {
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
}
