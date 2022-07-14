package gregtech.common.metatileentities.transport;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityPipelineEndpoint;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.transport.LongDistanceItemPipeBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MetaTileEntityPipelineItem extends MetaTileEntityPipelineEndpoint implements IItemHandler {

    private LongDistItemPipeWalker walker;

    public MetaTileEntityPipelineItem(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPipelineItem(metaTileEntityId);
    }

    @Override
    protected void scanPipes() {
        Pair<MetaTileEntityPipelineEndpoint, Integer> endpoint = scanPipes(getWorld(), getPos(), getFrontFacing(), state -> state.getBlock() instanceof LongDistanceItemPipeBlock, endpoint1 -> endpoint1 instanceof MetaTileEntityPipelineItem);
        if (endpoint != null) {
            this.target = endpoint.getKey();
            this.targetDistance = endpoint.getValue();
            this.target.setSource(this);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == getFrontFacing()) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected int getMinimumEndpointDistance() {
//        return 64; //TODO when done with testing
        return 0;
    }

    @Override
    protected LongDistancePipeWalker getPipeWalker() {
        if (walker == null || !GTUtility.arePosEqual(walker.getStartPos(), getPos())) {
            walker = new LongDistItemPipeWalker(getWorld(), getPos(), 0);
        }
        return walker;
    }

    @Nullable
    public IItemHandler getTargetInventory() {
        MetaTileEntityPipelineEndpoint target = getTargetEndpoint();
        if (target == null) return null;
        BlockPos invPos = target.getPos().offset(target.getFrontFacing().getOpposite());
        TileEntity te = getWorld().getTileEntity(invPos);
        if (te == null) return null;
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.getFrontFacing());
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

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
        IItemHandler handler = getTargetInventory();
        if (handler != null) {
            ItemStack remainder = handler.insertItem(i, itemStack, b);
            //GTLog.logger.info("Inserted {} to {}. Remainder {}", itemStack, handler.getClass().getSimpleName(), remainder);
            return remainder;
        }
        BlockPos invPos = target == null ? BlockPos.ORIGIN : target.getPos().offset(target.getFrontFacing().getOpposite());
        //GTLog.logger.info("No Inventory at {}, {}, {}", invPos.getX(), invPos.getY(), invPos.getZ());
        return itemStack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }
}
