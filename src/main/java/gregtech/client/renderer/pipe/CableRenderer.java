package gregtech.client.renderer.pipe;

import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.GTValues;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.Insulation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public class CableRenderer extends PipeRenderer {

    public static final CableRenderer INSTANCE = new CableRenderer();
    private final TextureAtlasSprite[] insulationTextures = new TextureAtlasSprite[6];
    private TextureAtlasSprite wireTexture;

    private CableRenderer() {
        super("gt_cable", new ResourceLocation(GTValues.MODID, "cable"));
    }

    @Override
    public void registerIcons(TextureMap map) {
        ResourceLocation wireLocation = new ResourceLocation(GTValues.MODID, "blocks/cable/wire");
        this.wireTexture = map.registerSprite(wireLocation);
        for (int i = 0; i < insulationTextures.length; i++) {
            ResourceLocation location = new ResourceLocation(GTValues.MODID, "blocks/cable/insulation_" + i);
            this.insulationTextures[i] = map.registerSprite(location);
        }
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof Insulation)) {
            return;
        }

        int insulationLevel = ((Insulation) pipeType).insulationLevel;
        IVertexOperation wireRender = new IconTransformation(wireTexture);
        ColourMultiplier wireColor = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()));
        ColourMultiplier insulationColor = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(0x404040));
        if (pipeTile != null) {
            if (pipeTile.getPaintingColor() != pipeTile.getDefaultPaintingColor()) {
                wireColor.colour = GTUtility.convertRGBtoOpaqueRGBA_CL(pipeTile.getPaintingColor());
            }
            insulationColor.colour = GTUtility.convertRGBtoOpaqueRGBA_CL(pipeTile.getPaintingColor());
        }

        if (insulationLevel != -1) {

            if ((renderContext.getConnections() & 63) == 0) {
                // render only insulation when cable has no connections
                renderContext.addOpenFaceRender(false, new IconTransformation(insulationTextures[5]), insulationColor);
                return;
            }

            renderContext.addOpenFaceRender(false, wireRender, wireColor)
                    .addOpenFaceRender(false, new IconTransformation(insulationTextures[insulationLevel]), insulationColor)
                    .addSideRender(false, new IconTransformation(insulationTextures[5]), insulationColor);
        } else {
            renderContext.addOpenFaceRender(false, wireRender, wireColor)
                    .addSideRender(false, wireRender, wireColor);
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return null;
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<?, ?> pipeTile) {
        if (pipeTile == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        IPipeType<?> pipeType = pipeTile.getPipeType();
        if (!(pipeType instanceof Insulation)) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        Material material = pipeTile instanceof TileEntityMaterialPipeBase ? ((TileEntityMaterialPipeBase<?, ?>) pipeTile).getPipeMaterial() : null;

        TextureAtlasSprite atlasSprite;
        int particleColor;
        int insulationLevel = ((Insulation) pipeType).insulationLevel;
        if (insulationLevel == -1) {
            atlasSprite = wireTexture;
            particleColor = material == null ? 0xFFFFFF : material.getMaterialRGB();
        } else {
            atlasSprite = insulationTextures[5];
            particleColor = pipeTile.getPaintingColor();
        }
        return Pair.of(atlasSprite, particleColor);
    }
}
