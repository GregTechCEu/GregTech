package gregtech.common.pipelikeold.fluidpipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    @Override
    public void update() {
        getCoverableImplementation().update();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
