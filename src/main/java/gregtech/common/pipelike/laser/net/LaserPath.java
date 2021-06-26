package gregtech.common.pipelike.laser.net;
import net.minecraft.util.math.BlockPos;
import gregtech.common.pipelike.laser.tile.LaserProperties;

import java.util.HashMap;

public class LaserPath {
    public BlockPos destination;
    public HashMap<BlockPos, LaserProperties> path = new HashMap<>();
    public int maxParallel = Integer.MAX_VALUE;
    public int minLaser = Integer.MAX_VALUE;

    public LaserPath cloneAndCompute(BlockPos destination) {
        LaserPath newPath = new LaserPath();
        newPath.path = new HashMap<>(path);
        newPath.destination = destination;
        for (LaserProperties opticalFiberProperties : path.values()) {
            newPath.maxParallel = Math.min(newPath.maxParallel, opticalFiberProperties.parallel);
            newPath.minLaser = Math.min(newPath.minLaser, opticalFiberProperties.laserVoltage);
        }
        return newPath;
    }
}
