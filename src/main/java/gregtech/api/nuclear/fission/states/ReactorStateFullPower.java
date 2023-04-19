package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateFullPower extends ReactorStateBase{

    public ReactorStateFullPower(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.powerProductionFactor < 0.6 * this.reactor.maxPower) {
            return new ReactorStateRampDown(this.reactor);
        } else if (this.reactor.fuelDepletion == 0) {
            return new ReactorStateShutdown(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor = responseFunction(this.reactor.maxPower, this.reactor.powerProductionFactor, this.reactor.criticalRodInsertion, this.reactor.controlRodInsertion);
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.neutronPoisoning *= 0.05D;
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
