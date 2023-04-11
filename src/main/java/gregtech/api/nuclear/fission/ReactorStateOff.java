package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateOff extends ReactorStateBase {

    public ReactorStateOff(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.controlRodInsertion <= this.reactor.criticalRodInsertion) {
            if (this.reactor.pressure == this.reactor.criticalPressure) {
                if (this.reactor.coolantFlowRate > this.reactor.criticalCoolantFlow()) {
                    if (this.reactor.voidFactor() == 0 && this.reactor.fuelDepletion < 1 && this.reactor.neutronPoisoning == 0) {
                        return new ReactorStateZeroPower(this.reactor);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void runStateEvolution() {
        // Reactor slowly cools down to ambient temperature while it is completely off
        this.reactor.temperature = Math.max(this.reactor.temperature * 0.95, this.reactor.envTemperature);
        // Neutron poisoning decays when the reactor is off
        this.reactor.neutronPoisoning = Math.max(this.reactor.neutronPoisoning * 0.95, 0.D);

    }
}
