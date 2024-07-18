package gregtech.api.graphnet.traverse.util;

public class MultLossOperator implements ReversibleLossOperator {

    private final double mult;

    public MultLossOperator(double mult) {
        assert mult > 0 && mult <= 1;
        this.mult = mult;
    }

    @Override
    public double applyLoss(double value) {
        return value * mult;
    }

    @Override
    public double undoLoss(double value) {
        return  value / mult;
    }
}
