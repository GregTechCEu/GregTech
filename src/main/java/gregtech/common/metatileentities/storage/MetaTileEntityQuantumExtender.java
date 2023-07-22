package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IDualHandler;
import gregtech.api.capability.IQuantumController;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetaTileEntityQuantumExtender extends MetaTileEntityQuantumStorage<IDualHandler> {

    IDualHandler handler = null;
    public MetaTileEntityQuantumExtender(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumExtender(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        // todo make a unique texture
        if (isConnected()) {
            Textures.ADVANCED_COMPUTER_CASING.render(renderState, translation, pipeline); // testing
        } else {
            Textures.SOLID_STEEL_CASING.render(renderState, translation, pipeline);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void setConnected(IQuantumController controller) {
        super.setConnected(controller);
        this.handler = getController().getHandler();
    }

    @Override
    public void setDisconnected() {
        super.setDisconnected();
        this.handler = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (getController() == null && this.handler == null) return super.getCapability(capability, side);

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) getTypeValue();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Type getType() {
        return Type.EXTENDER;
    }

    @Override
    public IDualHandler getTypeValue() {
        return getController().getHandler();
    }

    private class ExtenderHandler implements IDualHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return handler.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return handler.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return handler.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return handler.drain(maxDrain, doDrain);
        }

        @Override
        public int getSlots() {
            return handler.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return handler.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return handler.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return handler.getSlotLimit(slot);
        }

        @Override
        public boolean hasFluidTanks() {
            return handler.hasFluidTanks();
        }

        @Override
        public boolean hasItemHandlers() {
            return handler.hasItemHandlers();
        }
    }
}
