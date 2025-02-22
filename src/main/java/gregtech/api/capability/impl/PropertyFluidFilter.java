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
    private final int minFluidTemperature;
    private final boolean gasProof;
    private final boolean plasmaProof;

    public PropertyFluidFilter(int maxFluidTemperature, int minFluidTemperature,
                               boolean gasProof,
                               boolean acidProof,
                               boolean plasmaProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.minFluidTemperature = minFluidTemperature;
        this.gasProof = gasProof;
        if (acidProof) setCanContain(FluidAttributes.ACID, true);
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
    public int getMinFluidTemperature() {
        return minFluidTemperature;
    }

    @Override
    public boolean isGasProof() {
        return this.gasProof;
    }

    @Override
    public boolean isPlasmaProof() {
        return this.plasmaProof;
    }

    @Override
    public String toString() {
        return "PropertyFluidFilter{" +
                "containmentPredicate=" + containmentPredicate +
                ", maxFluidTemperature=" + maxFluidTemperature +
                ", minFluidTemperature=" + minFluidTemperature +
                ", gasProof=" + gasProof +
                ", plasmaProof=" + plasmaProof +
                '}';
    }
}
