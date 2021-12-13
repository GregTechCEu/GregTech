package gregtech.common.pipelike.cable.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.PerTickLongCounter;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.EnergyNetHandler;
import gregtech.common.pipelike.cable.net.WorldENet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties> {

    public TileEntityCable() {
        super();
        this.insulationColor = ConfigHolder.machines.defaultInsulationColor;
    }

    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    private final PerTickLongCounter amperageCounter = new PerTickLongCounter(0);
    private final PerTickLongCounter voltageCounter = new PerTickLongCounter(0);

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    public boolean checkAmperage(long amps) {
        return getMaxAmperage() >= amperageCounter.get(getWorld()) + amps;
    }

    public void incrementAmperage(long amps, long voltage) {
        if(voltage > voltageCounter.get(getWorld())) {
            voltageCounter.set(getWorld(), voltage);
        }
        amperageCounter.increment(getWorld(), amps);
    }

    public long getCurrentAmperage() {
        return amperageCounter.get(getWorld());
    }

    public long getCurrentVoltage() {
        return voltageCounter.get(getWorld());
    }

    public long getMaxAmperage() {
        return getNodeData().amperage;
    }

    public long getMaxVoltage() {
        return getNodeData().voltage;
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(new EnergyNetHandler(getEnergyNet(), this, facing));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    private EnergyNet getEnergyNet() {
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
    }


}
