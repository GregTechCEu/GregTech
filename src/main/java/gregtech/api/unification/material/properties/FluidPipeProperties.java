package gregtech.api.unification.material.properties;

import gregtech.api.capability.FluidContainmentInfo;
import gregtech.api.fluids.info.FluidTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public class FluidPipeProperties extends FluidContainmentInfo implements IMaterialProperty<FluidPipeProperties> {

    private int throughput;
    private final int tanks;

    public FluidPipeProperties(int throughput, @Nonnull FluidContainmentInfo info) {
        this(1, throughput, info);
    }

    /**
     * Should only be called from {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int tanks, int throughput, @Nonnull FluidContainmentInfo info) {
        this(tanks, throughput, info.canHoldLiquids(), info.canHoldGases(), info.canHoldPlasmas(),
                info.canHoldCryogenics(), info.canHoldAcids(), info.canHoldSuperacids(), info.getMaxTemperature());
    }

    public FluidPipeProperties(int throughput, boolean canHoldLiquids, boolean canHoldGases, boolean canHoldPlasmas,
                               boolean canHoldCryogenics, boolean canHoldAcids, boolean canHoldSuperacids, int maxTemperature) {
        this(1, throughput, canHoldLiquids, canHoldGases, canHoldPlasmas, canHoldCryogenics, canHoldAcids, canHoldSuperacids, maxTemperature);
    }

    public FluidPipeProperties(int tanks, int throughput, boolean canHoldLiquids, boolean canHoldGases, boolean canHoldPlasmas,
                               boolean canHoldCryogenics, boolean canHoldAcids, boolean canHoldSuperacids, int maxTemperature) {
        this(tanks, throughput, canHoldLiquids, canHoldGases, canHoldPlasmas, canHoldCryogenics, canHoldAcids, canHoldSuperacids, maxTemperature, null);
    }

    public FluidPipeProperties(int tanks, int throughput, boolean canHoldLiquids, boolean canHoldGases, boolean canHoldPlasmas,
                               boolean canHoldCryogenics, boolean canHoldAcids, boolean canHoldSuperacids, int maxTemperature,
                               @Nullable Set<FluidTag> allowedTags) {
        super(canHoldLiquids, canHoldGases, canHoldPlasmas, canHoldCryogenics, canHoldAcids, canHoldSuperacids, maxTemperature, allowedTags);
        this.tanks = tanks;
        this.throughput = throughput;
    }

        /**
         * Default property constructor.
         */
    public FluidPipeProperties() {
        this(1, false, false, false, false, false, false, 300);
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
        return maxTemperature;
    }

    public void setMaxFluidTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public boolean isLiquidProof() {
        return canHoldLiquids;
    }

    public void setLiquidProof(boolean canHoldLiquids) {
        this.canHoldLiquids = canHoldLiquids;
    }

    public boolean isGasProof() {
        return canHoldGases;
    }

    public void setGasProof(boolean canHoldGases) {
        this.canHoldGases = canHoldGases;
    }

    public boolean isPlasmaProof() {
        return canHoldPlasmas;
    }

    public void setPlasmaProof(boolean plasmaProof) {
        this.canHoldPlasmas = plasmaProof;
    }

    public boolean isCryoProof() {
        return canHoldCryogenics;
    }

    public void setCryoProof(boolean canHoldCryogenics) {
        this.canHoldCryogenics = canHoldCryogenics;
    }

    public boolean isAcidProof() {
        return canHoldAcids;
    }

    public void setAcidProof(boolean canHoldAcids) {
        this.canHoldAcids = canHoldAcids;
    }

    public boolean isSuperAcidProof() {
        return canHoldSuperacids;
    }

    public void setSuperAcidProof(boolean canHoldSuperacids) {
        this.canHoldSuperacids = canHoldSuperacids;
    }

    public void addAllowedData(@Nonnull FluidTag data) {
        this.allowedTags.add(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FluidPipeProperties that = (FluidPipeProperties) o;
        return getThroughput() == that.getThroughput() && getTanks() == that.getTanks() &&
                canHoldLiquids == that.isLiquidProof() && canHoldGases == that.isGasProof() &&
                canHoldPlasmas == that.isPlasmaProof() && canHoldCryogenics == that.isCryoProof() &&
                canHoldAcids == that.isAcidProof() && canHoldSuperacids == that.isSuperAcidProof() &&
                maxTemperature == that.getMaxFluidTemperature();
    }

    @Override
    public int hashCode() {
        //noinspection ObjectInstantiationInEqualsHashCode
        return Objects.hash(getThroughput(), getTanks(), canHoldLiquids, canHoldGases, canHoldPlasmas,
                canHoldCryogenics, canHoldAcids, canHoldSuperacids, maxTemperature);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "throughput=" + throughput +
                ", tanks=" + tanks +
                ", canHoldLiquids=" + canHoldLiquids +
                ", canHoldGases=" + canHoldGases +
                ", canHoldPlasmas=" + canHoldPlasmas +
                ", canHoldCryogenics=" + canHoldCryogenics +
                ", canHoldAcids=" + canHoldAcids +
                ", canHoldSuperacids=" + canHoldSuperacids +
                ", maxTemperature=" + maxTemperature +
                '}';
    }
}
