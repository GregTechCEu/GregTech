package gregtech.api.graphnet.group;

public enum MergeDirection {

    NONE,
    SOURCE,
    TARGET,
    NULL;

    public boolean allowsEdgeCreation() {
        return this != NONE;
    }

    public boolean source() {
        return this == SOURCE;
    }

    public boolean target() {
        return this == TARGET;
    }
}
