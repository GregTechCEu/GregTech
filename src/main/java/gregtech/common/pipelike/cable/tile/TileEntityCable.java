package gregtech.common.pipelike.cable.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.EnergyNetHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties> {

    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            EnergyNet net = (EnergyNet) getPipeNet();
            if (net == null) return null;
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(new EnergyNetHandler(net, this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    /*private EnergyNet getEnergyNet() {
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(getPos()))
            return currentEnergyNet; //return current net if it is still valid
        WorldENet worldENet = WorldENet.getWorldENet(getWorld());
        currentEnergyNet = worldENet.getNetFromPos(getPos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }*/
}
