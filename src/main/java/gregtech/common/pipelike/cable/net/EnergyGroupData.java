package gregtech.common.pipelike.cable.net;

import gregtech.api.pipenet.AbstractGroupData;

import gregtech.api.unification.material.properties.WireProperties;

import gregtech.common.pipelike.cable.Insulation;

import net.minecraft.world.World;

public class EnergyGroupData extends AbstractGroupData<Insulation, WireProperties> {

    private long lastEnergyFluxPerSec;
    private long energyFluxPerSec;
    private long lastTime;

    public long getEnergyFluxPerSec() {
        World world = this.group.net.getWorld();
        if (world != null && !world.isRemote && (world.getTotalWorldTime() - lastTime) >= 20) {
            lastTime = world.getTotalWorldTime();
            clearCache();
        }
        return lastEnergyFluxPerSec;
    }

    public void addEnergyFluxPerSec(long energy) {
        energyFluxPerSec += energy;
    }

    public void clearCache() {
        lastEnergyFluxPerSec = energyFluxPerSec;
        energyFluxPerSec = 0;
    }
}
