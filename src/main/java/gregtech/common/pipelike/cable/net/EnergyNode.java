package gregtech.common.pipelike.cable.net;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.nodenet.Node;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.PerTickLongCounter;
import net.minecraft.nbt.NBTTagCompound;

public class EnergyNode extends Node<WireProperties> {

    private final PerTickLongCounter amperageCounter = new PerTickLongCounter(0);
    private final PerTickLongCounter voltageCounter = new PerTickLongCounter(0);

    public EnergyNode(PipeNet<WireProperties> nodeNet) {
        super(nodeNet);
    }

    public boolean checkAmperage(long amps) {
        return getMaxAmperage() >= amperageCounter.get(getWorld()) + amps;
    }

    public void incrementAmperage(long amps, long voltage) {
        if (voltage > voltageCounter.get(getWorld())) {
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

    @Override
    public void transferNodeData(Node<WireProperties> oldNode) {
        EnergyNode oldEnergyNode = (EnergyNode) oldNode;
        amperageCounter.set(getWorld(), Math.max(amperageCounter.get(getWorld()), oldEnergyNode.amperageCounter.get(getWorld())));
        voltageCounter.set(getWorld(), Math.max(voltageCounter.get(getWorld()), oldEnergyNode.voltageCounter.get(getWorld())));
    }
}
