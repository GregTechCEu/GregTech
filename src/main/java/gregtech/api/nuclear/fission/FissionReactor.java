package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class FissionReactor {

    private IReactorState state;


    /**
     * Thresholds important for determining the evolution of the reactor
     */
    protected final int criticalRodInsertion;
    //protected final int criticalCoolantFlow;
    protected final int criticalPressure;
    protected final double criticalPower;

    /**
     * Integers used on variables with direct player control for easier adjustments
     */
    protected int controlRodInsertion;
    protected int coolantFlowRate;
    protected int pressure;

    protected double powerProductionFactor;
    protected double temperature;
    protected double fuelDepletion;
    protected double neutronPoisoning;

    protected double envTemperature;

    protected double maxTemperature;
    protected double maxPower;
    protected double maxCoolantFlow;

    protected double coolingFactor;

    protected double decayPower;

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
