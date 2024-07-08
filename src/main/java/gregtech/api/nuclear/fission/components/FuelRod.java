package gregtech.api.nuclear.fission.components;

import gregtech.api.unification.material.properties.FissionFuelProperty;

public class FuelRod extends ReactorComponent {

    private FissionFuelProperty fuel;

    public FuelRod(double maxTemperature, double thermalConductivity, FissionFuelProperty fuel, double mass) {
        super(0, maxTemperature, thermalConductivity, mass, true);
        this.fuel = fuel;
    }

    public double getDuration() {
        return fuel.getDuration();
    }

    public double getHEFissionFactor() {
        return fuel.getFastNeutronFissionCrossSection();
    }

    public double getLEFissionFactor() {
        return fuel.getSlowNeutronFissionCrossSection();
    }

    public double getHECaptureFactor() {
        return fuel.getFastNeutronCaptureCrossSection();
    }

    public double getLECaptureFactor() {
        return fuel.getSlowNeutronCaptureCrossSection();
    }

    public double getNeutronGenerationTime() {
        return fuel.getNeutronGenerationTime();
    }

    public FissionFuelProperty getFuel() {
        return fuel;
    }

    public void setFuel(FissionFuelProperty property) {
        this.fuel = property;
        this.maxTemperature = property.getMaxTemperature();
    }
}
