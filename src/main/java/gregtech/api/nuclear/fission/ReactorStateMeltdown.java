package gregtech.api.nuclear.fission;

public class ReactorStateMeltdown extends ReactorStateBase {

    public ReactorStateMeltdown(FissionReactor reactor) {
        super(reactor);
    }

    @Override
    public boolean isFailedState() {
        return true;
    }
}