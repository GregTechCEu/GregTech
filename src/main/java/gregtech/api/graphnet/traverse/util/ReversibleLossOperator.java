package gregtech.api.graphnet.traverse.util;

public interface ReversibleLossOperator {

    double applyLoss(double value);

    double undoLoss(double value);
}
