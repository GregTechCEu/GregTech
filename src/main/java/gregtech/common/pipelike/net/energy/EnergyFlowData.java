package gregtech.common.pipelike.net.energy;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record EnergyFlowData(long amperage, long voltage) {

    public long getEU() {
        return amperage * voltage;
    }
}
