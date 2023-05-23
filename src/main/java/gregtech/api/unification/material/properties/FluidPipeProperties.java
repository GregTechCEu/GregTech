package gregtech.api.unification.material.properties;

import gregtech.api.capability.IPropertyFluidFilter;

import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty<FluidPipeProperties>, IPropertyFluidFilter {

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

    @Override
    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public void setMaxFluidTemperature(int maxFluidTemperature) {
        this.maxFluidTemperature = maxFluidTemperature;
    }

    @Override
    public boolean isGasProof() {
        return gasProof;
    }

    public void setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
    }

    @Override
    public boolean isAcidProof() {
        return acidProof;
    }

    public void setAcidProof(boolean acidProof) {
        this.acidProof = acidProof;
    }

    @Override
    public boolean isCryoProof() {
        return cryoProof;
    }

    public void setCryoProof(boolean cryoProof) {
        this.cryoProof = cryoProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return plasmaProof;
    }

    public void setPlasmaProof(boolean plasmaProof) {
        this.plasmaProof = plasmaProof;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidPipeProperties that)) return false;
        return getThroughput() == that.getThroughput() &&
                getTanks() == that.getTanks() &&
                getMaxFluidTemperature() == that.getMaxFluidTemperature() &&
                isGasProof() == that.isGasProof() &&
                isAcidProof() == that.isAcidProof() &&
                isCryoProof() == that.isCryoProof() &&
                isPlasmaProof() == that.isPlasmaProof();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThroughput(), getTanks(), getMaxFluidTemperature(), isGasProof(), isAcidProof(), isCryoProof(), isPlasmaProof());
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "throughput=" + throughput +
                ", tanks=" + tanks +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", acidProof=" + acidProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                '}';
    }
}
