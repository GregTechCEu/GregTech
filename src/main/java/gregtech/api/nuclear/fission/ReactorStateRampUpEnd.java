package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateRampUpEnd extends ReactorStateBase {

    public ReactorStateRampUpEnd(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.controlRodInsertion == this.reactor.criticalRodInsertion && this.reactor.powerProductionFactor == this.reactor.maxPower) {
            return new ReactorStateFullPower(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        double speedFactor = 1.05D + (this.reactor.criticalRodInsertion - this.reactor.controlRodInsertion)/15.D;

        this.reactor.neutronPoisoning *= 0.05;
        this.reactor.powerProductionFactor *= speedFactor;
        this.reactor.powerProductionFactor = Math.min(this.reactor.powerProductionFactor, this.reactor.maxPower);
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
