package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.fission.states.IReactorState;

public class FissionReactor {

    private IReactorState state;

    /**
     * Thresholds important for determining the evolution of the reactor
     */
    public final int criticalRodInsertion;
    public final int criticalPressure;
    public final double criticalPower;

    /**
     * Integers used on variables with direct player control for easier adjustments
     */
    public int controlRodInsertion;
    public int coolantFlowRate;
    public int pressure;

    public double powerProductionFactor;
    public double temperature;
    public double fuelDepletion;
    public double neutronPoisoning;

    public double envTemperature;

    public double maxTemperature;
    public double maxPower;

    public double coolingFactor;

    public double decayPower;

    public FissionReactor(int criticalRodInsertion, int criticalPressure, double criticalPower) {
        this.criticalRodInsertion = criticalRodInsertion;
        this.criticalPower = criticalPower;
        this.criticalPressure = criticalPressure;
    }

    public boolean canCoolantBoil() {
        return false;
    }

    public boolean explosionPossible() {
        return false;
    }

    public double voidFactor() {
        return this.canCoolantBoil() ? (this.temperature - this.envTemperature) / (double) this.pressure : 0.D;
    }

    public double criticalCoolantFlow() {
        return this.powerProductionFactor / this.coolingFactor;
    }

}
