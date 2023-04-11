package gregtech.api.nuclear.fission;

import gregtech.api.nuclear.IReactorState;

public class ReactorStateBase implements IReactorState {

    protected FissionReactor reactor;

    public ReactorStateBase (FissionReactor reactor) {
        this.reactor = reactor;
    }

    public static boolean relaxedEquals(double x, double y, double tolerance) {
        return Math.abs(x - y) <= tolerance;
    }

    public static double responseFunction(double target, double value, double critical_rate, double rate) {
        return value * critical_rate/rate * Math.sqrt(target/value);
    }

    @Override
    public IReactorState getNextState() {
        return this;
    }

    @Override
    public void performWorldEffects() {

    }

    @Override
    public void runStateEvolution() {

    }
}
