package gregtech.api.unification.material.properties;

import java.util.Objects;

public class FluidPipeProperties {

    public final int maxFluidTemperature;
    public final int throughput;
    public final boolean gasProof;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidPipeProperties)) return false;
        FluidPipeProperties that = (FluidPipeProperties) o;
        return maxFluidTemperature == that.maxFluidTemperature &&
            throughput == that.throughput;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFluidTemperature, throughput);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
            ", maxFluidTemperature=" + maxFluidTemperature +
            ", throughput=" + throughput +
            '}';
    }
}
