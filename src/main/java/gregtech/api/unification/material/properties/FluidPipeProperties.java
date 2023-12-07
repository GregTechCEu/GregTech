package gregtech.api.unification.material.properties;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Objects;

public class FluidPipeProperties implements IMaterialProperty, IPropertyFluidFilter {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    private int throughput;
    private final int tanks;

    private int maxFluidTemperature;
    private boolean gasProof;
    private boolean cryoProof;
    private boolean plasmaProof;

    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof) {
        this(maxFluidTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, 1);
    }

    /**
     * Should only be called from
     * {@link gregtech.common.pipelike.fluidpipe.FluidPipeType#modifyProperties(FluidPipeProperties)}
     */
    public FluidPipeProperties(int maxFluidTemperature, int throughput, boolean gasProof, boolean acidProof,
                               boolean cryoProof, boolean plasmaProof, int tanks) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.throughput = throughput;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
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
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }

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
    public boolean canContain(@NotNull FluidState state) {
        return switch (state) {
            case LIQUID -> true;
            case GAS -> gasProof;
            case PLASMA -> plasmaProof;
        };
    }

    @Override
    public boolean canContain(@NotNull FluidAttribute attribute) {
        return containmentPredicate.getBoolean(attribute);
    }

    @Override
    public void setCanContain(@NotNull FluidAttribute attribute, boolean canContain) {
        this.containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    public boolean isGasProof() {
        return gasProof;
    }

    public void setGasProof(boolean gasProof) {
        this.gasProof = gasProof;
    }

    public boolean isAcidProof() {
        return canContain(FluidAttributes.ACID);
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
        if (!(o instanceof FluidPipeProperties that)) return false;
        return getThroughput() == that.getThroughput() &&
                getTanks() == that.getTanks() &&
                getMaxFluidTemperature() == that.getMaxFluidTemperature() &&
                isGasProof() == that.isGasProof() &&
                isCryoProof() == that.isCryoProof() &&
                isPlasmaProof() == that.isPlasmaProof() &&
                containmentPredicate.equals(that.containmentPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getThroughput(), getTanks(), getMaxFluidTemperature(), gasProof, cryoProof, plasmaProof,
                containmentPredicate);
    }

    @Override
    public String toString() {
        return "FluidPipeProperties{" +
                "throughput=" + throughput +
                ", tanks=" + tanks +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }
}
