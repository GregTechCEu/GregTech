package gregtech.client.renderer.pipe;

import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.optical.OpticalPipeType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class OpticalPipeRenderer extends PipeRenderer {

    public static final OpticalPipeRenderer INSTANCE = new OpticalPipeRenderer();
    private final EnumMap<OpticalPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(OpticalPipeType.class);

    private OpticalPipeRenderer() {
        super("gt_optical_pipe", new ResourceLocation(GTValues.MODID, "optical_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(OpticalPipeType.NORMAL, Textures.PIPE_NORMAL); //TODO make texture
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (pipeType instanceof OpticalPipeType) {
            renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                    .addSideRender(new IconTransformation(Textures.PIPE_SIDE)); //TODO make texture
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.PIPE_SIDE; //TODO make texture
    }
}
