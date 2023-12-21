package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class MetaTileEntityQuantumEnergyAcceptor extends MetaTileEntityQuantumStorage<IEnergyContainer> {

    public MetaTileEntityQuantumEnergyAcceptor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public Type getType() {
        return Type.ENERGY;
    }

    @Override
    public IEnergyContainer getTypeValue() {
        return getController() == null ? null : getController().getEnergyContainer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return (T) getTypeValue();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumEnergyAcceptor(metaTileEntityId);
    }
}
