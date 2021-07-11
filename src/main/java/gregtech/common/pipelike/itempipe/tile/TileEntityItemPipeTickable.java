package gregtech.common.pipelike.itempipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements ITickable {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private int transferredItems = 0;

    @Override
    public void update() {
        getCoverableImplementation().update();
        if(getTickTimer() % 20 == 0) {
            transferredItems = 0;
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    public int checkTransferableItems(int max, int amount) {
        return Math.max(0, Math.min(max - transferredItems, amount));
    }

    public void transferItems(int amount) {
        transferredItems += amount;
    }

}
