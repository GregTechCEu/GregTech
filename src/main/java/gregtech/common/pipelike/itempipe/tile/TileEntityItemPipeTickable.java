package gregtech.common.pipelike.itempipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements ITickable {

    @Override
    public void update() {
        getCoverableImplementation().update();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
