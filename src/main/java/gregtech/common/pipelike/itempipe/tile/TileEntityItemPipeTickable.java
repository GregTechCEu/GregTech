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

    @Override
    public void update() {
        getCoverableImplementation().update();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

}
