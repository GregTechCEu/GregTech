package gregtech.api.nuclear.fission;

public class ReactorStateExplosion extends ReactorStateBase {

    public ReactorStateExplosion(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public boolean isFailedState() {
        return true;
    }
}
