package gregtech.common.pipelike.optical.tile;

import net.minecraft.util.ITickable;

public class TileEntityOpticalPipeTickable extends TileEntityOpticalPipe implements ITickable {

    @Override
    public void update() {}

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
