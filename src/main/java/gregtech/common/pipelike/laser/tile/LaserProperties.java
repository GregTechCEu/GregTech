package gregtech.common.pipelike.laser.tile;
import gregtech.api.unification.ore.OrePrefix;

import java.util.Objects;

public class LaserProperties{
    public final int laserVoltage;
    public final int parallel;

    public LaserProperties(int laserVoltage, int baseAmperage) {
        this.laserVoltage = laserVoltage;
        this.parallel = baseAmperage;
    }

    @Override
    public boolean equals(Object i) {
        if (this == i) return true;
        if (!(i instanceof LaserProperties)) return false;
        LaserProperties that = (LaserProperties) i;
        return laserVoltage == that.laserVoltage &&
                parallel == that.parallel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(laserVoltage, parallel);
    }
}
