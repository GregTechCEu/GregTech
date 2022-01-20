package gregtech.client.renderer.pipe;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class FluidPipeRenderer extends PipeRenderer {

    public static final FluidPipeRenderer INSTANCE = new FluidPipeRenderer();
    private final Map<MaterialIconSet, EnumMap<FluidPipeType, PipeTextureInfo>> pipeTextures = new HashMap<>();

    private FluidPipeRenderer() {
        super("gt_fluid_pipe", new ResourceLocation(GTValues.MODID, "fluid_pipe"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            EnumMap<FluidPipeType, PipeTextureInfo> pipeTypeMap = new EnumMap<>(FluidPipeType.class);
            TextureAtlasSprite sideTexture = map.registerSprite(new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + iconSet.name + "/pipe_side"));
            for (FluidPipeType itemPipeType : FluidPipeType.values()) {
                ResourceLocation inLocation = new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + iconSet.name + "/pipe_" + itemPipeType.name + "_in");

                TextureAtlasSprite inTexture = map.registerSprite(inLocation);
                pipeTypeMap.put(itemPipeType, new PipeTextureInfo(inTexture, sideTexture));
            }
            this.pipeTextures.put(iconSet, pipeTypeMap);
        }
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof FluidPipeType)) {
            return;
        }
        PipeTextureInfo textureInfo = this.pipeTextures.get(material.getMaterialIconSet()).get(pipeType);
        renderContext.addOpenFaceRender(new IconTransformation(textureInfo.inTexture))
                .addSideRender(new IconTransformation(textureInfo.sideTexture));
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof FluidPipeType))
            return TextureUtils.getMissingSprite();
        return pipeTextures.get(material.getMaterialIconSet()).get(pipeType).sideTexture;
    }
}
