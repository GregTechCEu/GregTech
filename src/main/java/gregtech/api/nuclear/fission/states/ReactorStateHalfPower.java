package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateHalfPower extends ReactorStateBase {

    public ReactorStateHalfPower(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.temperature > this.reactor.maxTemperature) {
            // Lack of coolant or too high temperature causes meltdown
            return new ReactorStateOverheating(this.reactor);
        } else if (this.reactor.controlRodInsertion < this.reactor.criticalRodInsertion) {
            if (this.reactor.neutronPoisoning <= 0) {
                // If the reactor is not poisoned the final phase of the ramp up can begin
                return new ReactorStateRampUpEnd(this.reactor);
            } else {
                // Otherwise, stalls
                return new ReactorStateStalling(this.reactor);
            }
        } else {
            return this;
        }
    }

    @Override
    public void runStateEvolution() {
        this.reactor.neutronPoisoning = Math.abs(this.reactor.neutronPoisoning) > 0.05 ? this.reactor.neutronPoisoning * 0.95 : 0.D;
        this.reactor.temperature = responseFunction(this.reactor.maxTemperature, this.reactor.temperature, this.reactor.criticalCoolantFlow(), this.reactor.coolantFlowRate);
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }

}
