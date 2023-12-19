package gregtech.api.capability.impl;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public class PropertyFluidFilter implements IPropertyFluidFilter {

    private final Object2BooleanMap<FluidAttribute> containmentPredicate = new Object2BooleanOpenHashMap<>();

    private final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;

    public PropertyFluidFilter(int maxFluidTemperature,
                               boolean gasProof,
                               boolean acidProof,
                               boolean cryoProof,
                               boolean plasmaProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
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
        containmentPredicate.put(attribute, canContain);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containmentPredicate.keySet();
    }

    @Override
    public int getMaxFluidTemperature() {
        return this.maxFluidTemperature;
    }

    @Override
    public boolean isGasProof() {
        return this.gasProof;
    }

    @Override
    public boolean isCryoProof() {
        return this.cryoProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return this.plasmaProof;
    }

    @Override
    public String toString() {
        return "SimplePropertyFluidFilter{" +
                "maxFluidTemperature=" + maxFluidTemperature +
                ", gasProof=" + gasProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                ", containmentPredicate=" + containmentPredicate +
                '}';
    }
}
