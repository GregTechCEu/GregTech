package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateRampDown extends ReactorStateBase {

    public ReactorStateRampDown(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.controlRodInsertion < this.reactor.criticalRodInsertion) {
            return new ReactorStateStalling(this.reactor);
        } else if (this.reactor.powerProductionFactor < 0.1 * this.reactor.maxPower) {
            return new ReactorStateShutdown(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.powerProductionFactor -= 0.05 * this.reactor.powerProductionFactor;
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
        this.reactor.neutronPoisoning += Math.max(0.1, this.reactor.neutronPoisoning * 0.05);
        this.reactor.neutronPoisoning = Math.max(1.D, this.reactor.neutronPoisoning);
    }
}
