package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateOverheating extends ReactorStateBase {

    public ReactorStateOverheating(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature * 1.05) {
            return new ReactorStateMeltdown(this.reactor);
        } else if (this.reactor.controlRodInsertion >= this.reactor.criticalRodInsertion && this.reactor.temperature < this.reactor.maxTemperature) {
            return new ReactorStateRampUpBegin(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor = responseFunction(this.reactor.maxPower, this.reactor.powerProductionFactor, this.reactor.criticalRodInsertion, this.reactor.controlRodInsertion);
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
