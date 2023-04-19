package gregtech.api.nuclear.fission.states;

import gregtech.api.nuclear.fission.FissionReactor;

public class ReactorStateExplosion extends ReactorStateBase {

    public ReactorStateExplosion(FissionReactor reactor) {
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
