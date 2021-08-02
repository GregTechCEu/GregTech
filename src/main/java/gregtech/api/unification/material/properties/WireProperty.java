package gregtech.api.unification.material.properties;

import java.util.Objects;

public class WireProperty implements IMaterialProperty<WireProperty> {

    public final int voltage;
    public final int amperage;
    public final int lossPerBlock;

    public WireProperty(int voltage, int baseAmperage, int lossPerBlock) {
        this.voltage = voltage;
        this.amperage = baseAmperage;
        this.lossPerBlock = lossPerBlock;
    }

    /**
     * Default values constructor
     */
    public WireProperty() {
        this(8, 1, 1);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireProperty)) return false;
        WireProperty that = (WireProperty) o;
        return voltage == that.voltage &&
            amperage == that.amperage &&
            lossPerBlock == that.lossPerBlock;
    }

    @Override
    public int hashCode() {
        return Objects.hash(voltage, amperage, lossPerBlock);
    }
}
