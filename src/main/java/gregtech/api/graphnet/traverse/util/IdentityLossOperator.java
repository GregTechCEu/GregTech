package gregtech.api.graphnet.traverse.util;

public class IdentityLossOperator implements ReversibleLossOperator {
    public static final IdentityLossOperator INSTANCE = new IdentityLossOperator();

    private IdentityLossOperator() {}

    @Override
    public double applyLoss(double value) {
        return value;
    }

    @Override
    public double undoLoss(double value) {
        return value;
    }
}
