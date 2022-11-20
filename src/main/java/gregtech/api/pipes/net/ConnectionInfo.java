package gregtech.api.pipes.net;

public final class ConnectionInfo {
    public final Node target;
    public final int distance;

    ConnectionInfo(Node target, int distance) {
        this.target = target;
        this.distance = distance;
    }
}
