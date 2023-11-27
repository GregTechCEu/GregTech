package gregtech.common.pipelike.cable.tile;

import net.minecraft.util.ITickable;

public class TileEntityCableTickable extends TileEntityCable implements ITickable {

    public TileEntityCableTickable() {}

    @Override
    public void update() {
        getCoverableImplementation().update();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
