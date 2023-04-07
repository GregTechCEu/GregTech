package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

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
}
