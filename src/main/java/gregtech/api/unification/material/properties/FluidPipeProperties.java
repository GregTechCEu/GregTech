package gregtech.api.unification.material.properties;

import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties> {

    private int throughput;
    private final int tanks;

    private int maxFluidTemperature;
    private boolean gasProof;
    private boolean acidProof;
    private boolean cryoProof;
    private boolean plasmaProof;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    /**
     * Should only be called from {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, int tanks) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.tanks = tanks;
    }

    /**
     * Default property constructor.
     */
    public FluidPipeProperties() {
        this(300, 1, false, false, false, false);
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

    public int getTanks() {
        return tanks;
    }

    public int getThroughput() {
        return throughput;
    }

    public void setThroughput(int throughput) {
        this.throughput = throughput;
    }

    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public void setMaxFluidTemperature(int maxFluidTemperature) {
        this.maxFluidTemperature = maxFluidTemperature;
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public void setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
    }

    public boolean isAcidProof() {
        return acidProof;
    }

    public void setAcidProof(boolean acidProof) {
        this.acidProof = acidProof;
    }

    public boolean isCryoProof() {
        return cryoProof;
    }

    public void setCryoProof(boolean cryoProof) {
        this.cryoProof = cryoProof;
    }

    public boolean isPlasmaProof() {
        return plasmaProof;
    }

    public void setPlasmaProof(boolean plasmaProof) {
        this.plasmaProof = plasmaProof;
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
                ", acidProof=" + acidProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", tanks=" + tanks +
                '}';
    }
}
