package gregtech.common.pipelikeold.cable.net;

import gregtech.api.graphnet.AbstractGroupData;

import net.minecraft.world.World;

public class EnergyGroupData extends AbstractGroupData {

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
