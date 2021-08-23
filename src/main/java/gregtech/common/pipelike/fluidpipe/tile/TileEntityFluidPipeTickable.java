package gregtech.common.pipelike.fluidpipe.tile;

import net.minecraft.util.ITickable;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements ITickable {

    @Override
    public void update() {
        getCoverable().update();
    }
}
