package gregtech.common.pipelike.itempipe.longdistance;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityLongDistanceEndpoint;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class MetaTileEntityLDItemEndpoint extends MetaTileEntityLongDistanceEndpoint {

    private static final ItemStackHandler DEFAULT_INVENTORY = new ItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };

    public MetaTileEntityLDItemEndpoint(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, LDItemPipeType.INSTANCE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLDItemEndpoint(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (getWorld().isRemote || side != getFrontFacing() || !isInput()) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(DEFAULT_INVENTORY);
            }
            ILDEndpoint endpoint = getLink();
            if (endpoint != null) {
                EnumFacing outputFacing = endpoint.getOutputFacing();
                TileEntity te = endpoint.getNeighbor(outputFacing);
                if (te != null) {
                    T t = te.getCapability(capability, outputFacing.getOpposite());
                    if (t != null) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                                .cast(new ItemHandlerWrapper((IItemHandler) t));
                    }
                }
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(DEFAULT_INVENTORY);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.LV].render(renderState, translation, colouredPipeline);
        Textures.LD_ITEM_PIPE.renderOrientedState(renderState, translation, pipeline, frontFacing, false, false);
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing().getOpposite(), renderState, translation,
                pipeline);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), 0xFFFFFF);
    }

    public static class ItemHandlerWrapper extends ItemHandlerDelegate {

        public ItemHandlerWrapper(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }
}
