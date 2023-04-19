package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateZeroPower extends ReactorStateBase {

    public ReactorStateZeroPower(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        if (this.reactor.controlRodInsertion < this.reactor.criticalRodInsertion) {
            return new ReactorStateRampUpBegin(this.reactor);
        }
        return this;
    }

    @Override
    public void runStateEvolution() {
        this.reactor.powerProductionFactor = 0.1;
        this.reactor.fuelDepletion -= Math.max(0.D, this.reactor.powerProductionFactor/this.reactor.maxPower);
    }
}
