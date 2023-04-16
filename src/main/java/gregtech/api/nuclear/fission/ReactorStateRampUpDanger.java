package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateRampUpDanger extends ReactorStateBase {

    public ReactorStateRampUpDanger(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            return new ReactorStateMeltdown(this.reactor);
        } else if (this.reactor.controlRodInsertion >= 15) {
            if (this.reactor.coolantFlowRate == 0 && this.reactor.explosionPossible()) {
                return new ReactorStateExplosion(this.reactor);
            } else {
                return new ReactorStateStalled(this.reactor);
            }
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor *= 1.5;
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
