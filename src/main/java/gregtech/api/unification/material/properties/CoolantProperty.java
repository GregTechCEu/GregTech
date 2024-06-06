package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

public class CoolantProperty implements IMaterialProperty {

    private Material hotCoolant;
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
    private double specialCoolantAbsorption = 1;
    private boolean accumulatesHydrogen = false;

    public CoolantProperty(Material hotCoolant, Material hotHPCoolant, double moderatorFactor, double coolingFactor,
                           double boilingPoint, double absorption, double pressure, double heatOfVaporization,
                           double specificHeatCapacity) {
        this.hotCoolant = hotCoolant;
        this.hotHPCoolant = hotHPCoolant;
        this.moderatorFactor = moderatorFactor;
        this.coolingFactor = coolingFactor;
        this.boilingPoint = boilingPoint;
        this.absorption = absorption;
        this.pressure = pressure;
        this.heatOfVaporization = heatOfVaporization;
        this.specificHeatCapacity = specificHeatCapacity;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    public void setHotCoolant(Material hotCoolant) {
        this.hotCoolant = hotCoolant;
    }

    public Material getHotCoolant() {
        return this.hotCoolant;
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

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getPressure() {
        return pressure;
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

    public double getSpecialCoolantAbsorption() {
        return specialCoolantAbsorption;
    }

    /**
     * Used to adjust the amount of heat needed to heat the coolant from the ideal thermodynamic conditions; this is
     * really only for distilled water.
     * 
     * @param specialCoolantAbsorption A divisor to the amount of heat needed to heat the coolant from the ideal
     *                                 thermodynamic
     *                                 conditions.
     * @return The property itself.
     */
    public CoolantProperty setSpecialCoolantAbsorption(double specialCoolantAbsorption) {
        this.specialCoolantAbsorption = specialCoolantAbsorption;
        return this;
    }

    public boolean accumulatesHydrogen() {
        return accumulatesHydrogen;
    }

    public CoolantProperty setAccumulatesHydrogen(boolean accumulatesHydrogen) {
        this.accumulatesHydrogen = accumulatesHydrogen;
        return this;
    }
}
