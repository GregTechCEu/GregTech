package gregtech.api.graphnet.pipenet.traverse;

public interface ITileFlowManager {

    default boolean canAcceptFlow() {
        return getMaximumFlow() > 0;
    }

    long getMaximumFlow();

    void reportAttemptingFlow(long flow);

    long acceptFlow(long flow);
}
