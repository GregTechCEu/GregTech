package gregtech.api.unification.material.properties;

import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties> {

    // TODO These need to be made private, and done through getters/setters
    public int maxFluidTemperature;
    public int throughput;
    public boolean gasProof;
    public final int tanks;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof) {
        this(maxFluidTemperature, throughput, gasProof, 1);
    }

    /**
     * Should only be called from {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, int tanks) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        this.tanks = tanks;
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
            throughput == that.throughput && gasProof == that.gasProof && tanks == that.tanks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFluidTemperature, throughput, gasProof, tanks);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "maxFluidTemperature=" + maxFluidTemperature +
                ", throughput=" + throughput +
                ", gasProof=" + gasProof +
                ", tanks=" + tanks +
                '}';
    }
}
