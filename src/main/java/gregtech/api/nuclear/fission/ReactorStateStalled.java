package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateStalled extends ReactorStateBase {

    public ReactorStateStalled(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.controlRodInsertion < this.reactor.criticalRodInsertion) {
            return new ReactorStateRampUpDanger(this.reactor);
        } else if (this.reactor.controlRodInsertion >= 15 && Math.abs(this.reactor.neutronPoisoning) < 0.05) {
            return new ReactorStateShutdown(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor = Math.max(0.05 * this.reactor.maxPower, this.reactor.powerProductionFactor * 0.9);
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.neutronPoisoning *= 0.95;
    }
}
