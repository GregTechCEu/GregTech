package gregtech.api.graphnet.edge;

public interface FlowBufferTickProvider {

    int getFlowBufferTicks();

    default int getRegenerationTime() {
        return 1;
    }
}
