package gregtech.api.unification.material.properties;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;

import java.util.Objects;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;

public class WireProperties implements IMaterialProperty<WireProperties> {

    private int voltage;
    private int amperage;
    private int lossPerBlock;
    private boolean isSuperconductor;

    private boolean wireFromDust;

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock) {
        this(voltage, baseAmperage, lossPerBlock, false, false);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon) {
        this(voltage, baseAmperage, lossPerBlock, isSuperCon, false);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, boolean isSuperCon, boolean wireFromDust) {
        this.voltage = voltage;
        this.amperage = baseAmperage;
        this.lossPerBlock = isSuperCon ? 0 : lossPerBlock;
        this.isSuperconductor = isSuperCon;
        this.wireFromDust = wireFromDust;
    }

    /**
     * Default values constructor
     */
    public WireProperties() {
        this(8, 1, 1, false);
    }

    /**
     * Retrieves the current wire voltage
     *
     * @return The current wire voltage
     */
    public int getVoltage() {
        return voltage;
    }

    /**
     * Sets the current wire voltage
     *
     * @param voltage The new wire voltage
     */
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    /**
     * Retrieves the current wire amperage
     *
     * @return The current wire amperage
     */
    public int getAmperage() {
        return amperage;
    }

    /**
     * Sets the current wire amperage
     *
     * @param amperage The new current wire amperage
     */
    public void setAmperage(int amperage) {
        this.amperage = amperage;
    }

    /**
     * Retrieves the current wire loss per block
     *
     * @return The current wire loss per block
     */
    public int getLossPerBlock() {
        return lossPerBlock;
    }

    /**
     * Sets the current wire loss per block
     *
     * @param lossPerBlock The new wire loss per block
     */
    public void setLossPerBlock(int lossPerBlock) {
        this.lossPerBlock = lossPerBlock;
    }

    /**
     * If the current wire is a Superconductor wire
     *
     * @return {@code true} if the current wire is a Superconductor
     */
    public boolean isSuperconductor() {
        return isSuperconductor;
    }

    /**
     * If Wires of this material should be made from a Dust instead of an Ingot.
     * Intended for things like Graphene.
     */
    public boolean isWireFromDust() {
        return wireFromDust;
    }

    /**
     * Sets the current wire to a superconductor wire
     *
     * @param isSuperconductor The new wire superconductor status
     */
    public void setSuperconductor(boolean isSuperconductor) {
        this.isSuperconductor = isSuperconductor;
    }

    public void setWireFromDust(boolean wireFromDust) {
        this.wireFromDust = wireFromDust;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!isWireFromDust()) {
            properties.ensureSet(PropertyKey.INGOT, true);

            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor && voltage >= GTValues.V[GTValues.IV] && !thisMaterial.hasFlag(GENERATE_FOIL)) {
                thisMaterial.addFlags(GENERATE_FOIL);
            }
        } else {
            properties.ensureSet(PropertyKey.DUST, true);
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
