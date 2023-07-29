package gregtech.api.capability.impl;

import gregtech.api.capability.IPropertyFluidFilter;

public class PropertyFluidFilter implements IPropertyFluidFilter {

    private final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean acidProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;

    public PropertyFluidFilter(int maxFluidTemperature,
                               boolean gasProof,
                               boolean acidProof,
                               boolean cryoProof,
                               boolean plasmaProof) {
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
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
    public boolean isAcidProof() {
        return this.acidProof;
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
                ", acidProof=" + acidProof +
                ", cryoProof=" + cryoProof +
                ", plasmaProof=" + plasmaProof +
                '}';
    }
}
