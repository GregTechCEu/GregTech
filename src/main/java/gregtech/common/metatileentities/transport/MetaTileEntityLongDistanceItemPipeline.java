package gregtech.common.metatileentities.transport;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.LongDistanceItemPipelineBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

public class MetaTileEntityLongDistanceItemPipeline extends MetaTileEntityPipelineEndpoint {

    public MetaTileEntityLongDistanceItemPipeline(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLongDistanceItemPipeline(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(4);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(4);
    }

    @Override
    protected int getMinimumEndpointDistance() {
        return 0;
    }

    @Override
    protected boolean isPipeBlockValid(Block block) {
        return block instanceof LongDistanceItemPipelineBlock;
    }

    @Override
    protected boolean isSameConnector(MetaTileEntity metaTileEntity) {
        return metaTileEntity instanceof MetaTileEntityLongDistanceItemPipeline;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, pipeline);
        Textures.ITEM_PIPELINE_OVERLAY.renderOrientedState(renderState, translation, pipeline, frontFacing, false, false);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), getPaintingColor());
    }
}
