package gregtech.client.renderer.pipe;

import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class ItemPipeRenderer extends PipeRenderer {

    public static final ItemPipeRenderer INSTANCE = new ItemPipeRenderer();
    private final EnumMap<ItemPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(ItemPipeType.class);

    private ItemPipeRenderer() {
        super("gt_item_pipe", new ResourceLocation(GTValues.MODID, "item_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        pipeTextures.put(ItemPipeType.SMALL, Textures.PIPE_SMALL);
        pipeTextures.put(ItemPipeType.NORMAL, Textures.PIPE_NORMAL);
        pipeTextures.put(ItemPipeType.LARGE, Textures.PIPE_LARGE);
        pipeTextures.put(ItemPipeType.HUGE, Textures.PIPE_HUGE);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_SMALL, Textures.PIPE_SMALL);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_NORMAL, Textures.PIPE_NORMAL);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_LARGE, Textures.PIPE_LARGE);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_HUGE, Textures.PIPE_HUGE);
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof ItemPipeType)) {
            return;
        }
        renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                .addSideRender(new IconTransformation(Textures.PIPE_SIDE));

        if (((ItemPipeType) pipeType).isRestrictive()) {
            renderContext.addSideRender(false, new IconTransformation(Textures.RESTRICTIVE_OVERLAY));
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.PIPE_SIDE;
    }
}
