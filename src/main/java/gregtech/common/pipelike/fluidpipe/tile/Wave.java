package gregtech.common.pipelike.fluidpipe.tile;

import gregtech.api.util.GTLog;

public class Wave {

    private int id;
    private int useCount;

    public Wave(int id) {
        this(id, 0);
    }

    public Wave(int id, int useCount) {
        this.id = id;
        this.useCount = useCount;
    }

    public void reassignId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void addUser() {
        useCount++;
    }

    public boolean removeUser() {
        if(useCount == 1)
            GTLog.logger.info("[{}]Removing wave", id);
        return --useCount == 0;
    }

    public int getUseCount() {
        return useCount;
    }
}
