package gregtech.api.unification.material.properties;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;

import java.util.Objects;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;

public class WireProperties implements IMaterialProperty<WireProperties> {

    // TODO these need to be private and handled with getters/setters
    public int voltage;
    public int amperage;
    public int lossPerBlock;
    public boolean isSuperconductor;

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock) {
        this(voltage, baseAmperage, lossPerBlock, false);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon) {
        this.voltage = voltage;
        this.amperage = baseAmperage;
        this.lossPerBlock = isSuperCon ? 0 : lossPerBlock;
        this.isSuperconductor = isSuperCon;
    }

    /**
     * Default values constructor
     */
    public WireProperties() {
        this(8, 1, 1, false);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.INGOT, true);

        // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
        Material thisMaterial = properties.getMaterial();
        if (!isSuperconductor && voltage >= GTValues.V[GTValues.IV] && !thisMaterial.hasFlag(GENERATE_FOIL)) {
            thisMaterial.addFlags(GENERATE_FOIL);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireProperties)) return false;
        WireProperties that = (WireProperties) o;
        return voltage == that.voltage &&
                amperage == that.amperage &&
                lossPerBlock == that.lossPerBlock &&
                isSuperconductor == that.isSuperconductor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(voltage, amperage, lossPerBlock);
    }
}
