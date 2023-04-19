package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateMeltdown extends ReactorStateBase {

    public ReactorStateMeltdown(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public IReactorState getNextState() {
        return this;
    }

    @Override
    public boolean isFailedState() {
        return true;
    }
}
