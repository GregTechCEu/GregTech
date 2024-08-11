package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.AbstractGroupData;

public class EnergyGroupData extends AbstractGroupData {

    private final long[] lastEnergyFluxPerSec = new long[2];
    private final long[] energyFluxPerSec = new long[2];
    private long updateTime;

    public long[] getEnergyFluxPerSec(long queryTick) {
        updateCache(queryTick);
        return lastEnergyFluxPerSec;
    }

    public void addEnergyInPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyFluxPerSec[0] += energy;
    }

    public void addEnergyOutPerSec(long energy, long queryTick) {
        updateCache(queryTick);
        energyFluxPerSec[1] += energy;
    }

    private void updateCache(long queryTick) {
        if (queryTick > updateTime) {
            updateTime = updateTime + 20;
            clearCache();
        }
    }

    public void clearCache() {
        lastEnergyFluxPerSec[0] = energyFluxPerSec[0];
        lastEnergyFluxPerSec[1] = energyFluxPerSec[1];
        energyFluxPerSec[0] = 0;
        energyFluxPerSec[1] = 0;
    }
}
