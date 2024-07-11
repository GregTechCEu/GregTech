package gregtech.api.unification.material.properties;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;

import java.util.List;
import java.util.Objects;

import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;

public class WireProperties implements IMaterialProperty, IPipeNetData<WireProperties> {

    private int voltage;
    private int amperage;
    private int lossPerBlock;
    private int meltTemperature;
    private int superconductorCriticalTemperature;
    private boolean isSuperconductor;

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock) {
        this(voltage, baseAmperage, lossPerBlock, 0, false);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, int meltTemperature) {
        this(voltage, baseAmperage, lossPerBlock, meltTemperature, false);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, int meltTemperature, boolean isSuperCon) {
        this(voltage, baseAmperage, lossPerBlock, meltTemperature, isSuperCon, 0);
    }

    public WireProperties(int voltage, int baseAmperage, int lossPerBlock, int meltTemperature, boolean isSuperCon,
                          int criticalTemperature) {
        this.voltage = voltage;
        this.amperage = baseAmperage;
        this.lossPerBlock = isSuperCon ? 0 : lossPerBlock;
        this.meltTemperature = meltTemperature;
        this.superconductorCriticalTemperature = isSuperCon ? criticalTemperature : 0;
        this.isSuperconductor = isSuperCon;
    }

    /**
     * Default values constructor
     */
    public WireProperties() {
        this(8, 1, 1, 0, false);
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
    public int getLoss() {
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
     * Retrieves the current melt temperature.
     *
     * @return The current melt temperature
     */
    public int getMeltTemperature() {
        return meltTemperature == 0 ? 3000 : meltTemperature;
    }

    /**
     * Sets the current melt temperature
     *
     * @param meltTemperature The new melt temperature
     */
    public void setMeltTemperature(int meltTemperature) {
        this.meltTemperature = meltTemperature;
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
     * Sets the current wire to a superconductor wire
     *
     * @param isSuperconductor The new wire superconductor status
     */
    public void setSuperconductor(boolean isSuperconductor) {
        this.isSuperconductor = isSuperconductor;
    }

    /**
     * Retrieves the critical temperature of the superconductor (the temperature at which the superconductive phase
     * transition happens)
     *
     * @return Critical temperature of the material
     */
    public int getSuperconductorCriticalTemperature() {
        return superconductorCriticalTemperature;
    }

    /**
     * Sets the material's critical temperature
     *
     * @param criticalTemperature The new critical temperature
     */
    public void setSuperconductorCriticalTemperature(int criticalTemperature) {
        this.superconductorCriticalTemperature = this.isSuperconductor ? criticalTemperature : 0;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
        if (properties.hasProperty(PropertyKey.INGOT)) {
            // Ensure all Materials with Cables and voltage tier IV or above have a Foil for recipe generation
            Material thisMaterial = properties.getMaterial();
            if (!isSuperconductor && voltage >= GTValues.V[GTValues.IV] && !thisMaterial.hasFlag(GENERATE_FOIL)) {
                thisMaterial.addFlags(GENERATE_FOIL);
            }
        }
        if (this.meltTemperature == 0 && properties.hasProperty(PropertyKey.FLUID)) {
            // autodetermine melt temperature from registered fluid
            FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
            Fluid fluid = prop.getStorage().get(FluidStorageKeys.LIQUID);
            if (fluid == null) {
                FluidBuilder builder = prop.getStorage().getQueuedBuilder(FluidStorageKeys.LIQUID);
                if (builder != null) {
                    this.setMeltTemperature(builder.currentTemp());
                }
            } else {
                this.setMeltTemperature(fluid.getTemperature());
            }
        }
    }

    @Override
    public double getWeightFactor() {
        return this.getLoss() + 0.001 / this.getAmperage();
    }

    @Override
    public int getThroughput() {
        return this.getAmperage();
    }

    @Override
    public WireProperties getSumData(List<WireProperties> datas) {
        int amperage = this.getAmperage();
        int voltage = this.getVoltage();
        int loss = this.getLoss();
        for (WireProperties data : datas) {
            amperage = Math.min(amperage, data.getAmperage());
            voltage = Math.min(voltage, data.getVoltage());
            loss += data.getLoss();
        }
        return new WireProperties(voltage, amperage, loss);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WireProperties that = (WireProperties) o;
        return voltage == that.voltage && amperage == that.amperage && lossPerBlock == that.lossPerBlock &&
                meltTemperature == that.meltTemperature &&
                superconductorCriticalTemperature == that.superconductorCriticalTemperature &&
                isSuperconductor == that.isSuperconductor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(voltage, amperage, lossPerBlock, meltTemperature, superconductorCriticalTemperature,
                isSuperconductor);
    }
}
