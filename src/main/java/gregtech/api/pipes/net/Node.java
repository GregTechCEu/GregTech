package gregtech.api.pipes.net;

public class Node {
    private ConnectionInfo[] connections;
    private PipeNetwork parent = null;

    public Node(ConnectionInfo[] connections) {
        this.connections = connections;
    }

    public void setParent(PipeNetwork parent) {
        this.parent = parent;
    }

    public PipeNetwork getParent() {
        return this.parent;
    }

    public void setConnection(int side, ConnectionInfo connectionInfo) {
        this.connections[side] = connectionInfo;
    }

    public ConnectionInfo[] getConnections() {
        return this.connections;
    }

    public ConnectionInfo getConnection(int side) {
        return this.connections[side];
    }
}
