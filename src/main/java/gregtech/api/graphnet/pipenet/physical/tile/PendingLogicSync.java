package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.logic.NetLogicEntry;

public final class PendingLogicSync {

    private final int networkID;
    private final NetLogicEntry<?, ?> entry;
    private boolean removed;
    private boolean fullChange;

    public PendingLogicSync(int networkID, NetLogicEntry<?, ?> entry, boolean removed, boolean fullChange) {
        this.networkID = networkID;
        this.entry = entry;
        this.removed = removed;
        this.fullChange = fullChange;
    }

    public int networkID() {
        return networkID;
    }

    public NetLogicEntry<?, ?> entry() {
        return entry;
    }

    public void markUnremoved() {
        this.removed = false;
    }

    public void markRemoved() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void markFullChange() {
        this.fullChange = true;
    }

    public boolean isFullChange() {
        return fullChange;
    }
}
