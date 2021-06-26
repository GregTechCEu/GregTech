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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaserProperties)) return false;
        LaserProperties that = (LaserProperties) o;
        return laserVoltage == that.laserVoltage &&
                parallel == that.parallel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(laserVoltage, parallel, "optical_fiber");
    }
}
