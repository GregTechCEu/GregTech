package gregtech.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;

public class CoolantProperty implements IMaterialProperty {

    private Material hotHPCoolant;
    private double moderatorFactor;
    /**
     * Roughly the heat transfer coefficient
     * Do not put too much thought into this
     */
    private double coolingFactor;
    // in kelvin at standard conditions
    private double boilingPoint;
    // neutron absorption rate
    private double absorption;
    // in pascal
    private double pressure;
    // in J/L
    private double heatOfVaporization;
    // in J/(kg*K)
    private double specificHeatCapacity;
    private boolean accumulatesHydrogen = false;
    // To store the specific key
    private FluidStorageKey key;

    public CoolantProperty(Material hotHPCoolant, FluidStorageKey key, double moderatorFactor, double coolingFactor,
                           double boilingPoint, double absorption, double heatOfVaporization,
                           double specificHeatCapacity) {
        this.hotHPCoolant = hotHPCoolant;
        this.moderatorFactor = moderatorFactor;
        this.coolingFactor = coolingFactor;
        this.boilingPoint = boilingPoint;
        this.absorption = absorption;
        this.heatOfVaporization = heatOfVaporization;
        this.specificHeatCapacity = specificHeatCapacity;
        this.key = key;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    public void setHotHPCoolant(Material hotHPCoolant) {
        this.hotHPCoolant = hotHPCoolant;
    }

    public Material getHotHPCoolant() {
        return this.hotHPCoolant;
    }

    public void setModeratorFactor(double moderatorFactor) {
        this.moderatorFactor = moderatorFactor;
    }

    public double getModerationFactor() {
        return this.moderatorFactor;
    }

    public void setCoolingFactor(double coolingFactor) {
        this.coolingFactor = coolingFactor;
    }

    public double getCoolingFactor() {
        return this.coolingFactor;
    }

    public void setBoilingPoint(double boilingPoint) {
        this.boilingPoint = boilingPoint;
    }

    public double getBoilingPoint() {
        return this.boilingPoint;
    }

    public void setAbsorption(double absorption) {
        this.absorption = absorption;
    }

    public double getAbsorption() {
        return absorption;
    }

    public double getHeatOfVaporization() {
        return heatOfVaporization;
    }

    public void setHeatOfVaporization(double heatOfVaporization) {
        this.heatOfVaporization = heatOfVaporization;
    }

    public double getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    public void setSpecificHeatCapacity(double specificHeatCapacity) {
        this.specificHeatCapacity = specificHeatCapacity;
    }

    public boolean accumulatesHydrogen() {
        return accumulatesHydrogen;
    }

    public CoolantProperty setAccumulatesHydrogen(boolean accumulatesHydrogen) {
        this.accumulatesHydrogen = accumulatesHydrogen;
        return this;
    }

    public boolean isCorrectFluid(Material material, Fluid fluid) {
        return material.getFluid(key) == fluid;
    }
}
