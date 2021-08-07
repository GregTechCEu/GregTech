package gregtech.common.pipelike.cable.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.api.unification.material.properties.WireProperties;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties> {

    private IEnergyContainer energyContainer;

    private IEnergyContainer getEnergyContainer() {
        if (energyContainer == null) {
            energyContainer = new CableEnergyContainer(this);
        }
        return energyContainer;
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(getEnergyContainer());
        }
        return super.getCapabilityInternal(capability, facing);
    }

}
