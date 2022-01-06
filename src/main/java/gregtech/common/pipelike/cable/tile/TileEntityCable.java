package gregtech.common.pipelike.cable.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
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
import java.util.EnumMap;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperties> {

    private final EnumMap<EnumFacing, EnergyNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter(0);
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter(0, 20);
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter(0, 20);
    private EnergyNetHandler defaultHandler;
    // the EnergyNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IEnergyContainer clientCapability = IEnergyContainer.DEFAULT;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    private void initHandlers() {
        EnergyNet net = getEnergyNet();
        if (net == null) {
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            handlers.put(facing, new EnergyNetHandler(net, this, facing));
        }
        defaultHandler = new EnergyNetHandler(net, this, null);
    }

    /**
     * Should only be called internally
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(long amps, long voltage) {
        if (voltage > maxVoltageCounter.get(world)) {
            maxVoltageCounter.set(world, voltage);
        }
        averageVoltageCounter.increment(world, voltage);
        averageAmperageCounter.increment(world, amps);

        return getAverageAmperage() > getMaxAmperage();
    }

    public double getAverageAmperage() {
        return averageAmperageCounter.getAverage(getWorld());
    }

    public long getCurrentMaxVoltage() {
        return maxVoltageCounter.get(getWorld());
    }

    public double getAverageVoltage() {
        return averageVoltageCounter.getAverage(getWorld());
    }

    public long getMaxAmperage() {
        return getNodeData().getAmperage();
    }

    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    @Nullable
    @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if(world.isRemote)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(clientCapability);
            if (handlers.size() == 0)
                initHandlers();
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    private EnergyNet getEnergyNet() {
        if(world == null || world.isRemote)
            return null;
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

    @Override
    public int getDefaultPaintingColor() {
        return 0x404040;
    }
}
