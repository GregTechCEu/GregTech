package gregtech.common.pipelike.laser.tile;
import net.minecraft.util.ITickable;
import gregtech.api.pipenet.tile.IPipeTile;

public class TileEntityLaserTickable extends TileEntityLaser implements ITickable {

    public TileEntityLaserTickable() {
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

