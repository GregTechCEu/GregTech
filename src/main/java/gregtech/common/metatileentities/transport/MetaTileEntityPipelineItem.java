package gregtech.common.metatileentities.transport;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.LongDistanceItemPipelineBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

public class MetaTileEntityPipelineItem extends MetaTileEntityPipelineEndpoint {

    public MetaTileEntityPipelineItem(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPipelineItem(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(16, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(16, this, true);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) return;
        if (getOffsetTimer() % 20 == 0) {
            pullItemsFromNearbyHandlers(getFrontFacing());
            pushItemsIntoNearbyHandlers(getFrontFacing().getOpposite());

            // todo implement notifiable logic
            if (checkTargetValid()) GTTransferUtils.moveInventoryItems(getImportItems(), target.getExportItems());
        }
    }

    @Override
    protected int getMinimumEndpointDistance() {
//        return 64; //TODO when done with testing
        return 0;
    }

    @Override
    protected boolean isPipeBlockValid(Block block) {
        return block instanceof LongDistanceItemPipelineBlock;
    }

    @Override
    protected boolean isSameConnector(MetaTileEntity metaTileEntity) {
        return metaTileEntity instanceof MetaTileEntityPipelineItem;
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
