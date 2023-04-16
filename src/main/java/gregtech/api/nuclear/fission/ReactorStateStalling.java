package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateStalling extends ReactorStateBase {

    public ReactorStateStalling(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.neutronPoisoning == 1.) {
            return new ReactorStateStalled(this.reactor);
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor = Math.max(0.1 * this.reactor.maxPower, this.reactor.powerProductionFactor * 0.9);
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
        this.reactor.neutronPoisoning += 0.01;
    }
}
