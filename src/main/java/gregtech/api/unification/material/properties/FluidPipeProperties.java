package gregtech.api.unification.material.properties;

import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties> {

    public final int maxFluidTemperature;
    public final int throughput;
    public final boolean gasProof;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties() {
        this(300, 1, false);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);

        if (properties.hasProperty(PropertyKey.ITEM_PIPE)) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
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
                ", gasProof=" + gasProof +
                '}';
    }
}
