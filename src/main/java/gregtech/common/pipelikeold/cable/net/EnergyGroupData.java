package gregtech.common.pipelikeold.cable.net;

import gregtech.api.graphnet.AbstractGroupData;

import net.minecraftforge.fml.common.FMLCommonHandler;

public class EnergyGroupData extends AbstractGroupData {

    private long lastEnergyFluxPerSec;
    private long energyFluxPerSec;
    private long updateTime;

    public long getEnergyFluxPerSec() {
        updateCache();
        return lastEnergyFluxPerSec;
    }

    public void addEnergyFluxPerSec(long energy) {
        updateCache();
        energyFluxPerSec += energy;
    }

    private void updateCache() {
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        if (tick > updateTime) {
            updateTime = updateTime + 20;
            clearCache();
        }
    }

    public void clearCache() {
        lastEnergyFluxPerSec = energyFluxPerSec;
        energyFluxPerSec = 0;
    }
}
