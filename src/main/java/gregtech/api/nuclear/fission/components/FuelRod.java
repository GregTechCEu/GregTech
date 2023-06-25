package gregtech.api.nuclear.fission.components;

import gregtech.api.unification.material.properties.FissionFuelProperty;

public class FuelRod extends ReactorComponent {
    private final FissionFuelProperty fuel;
    private final double neutronSourceIntensity;

    public FuelRod(double maxTemperature, double thermalConductivity, FissionFuelProperty fuel, double neutronSourceIntensity) {
        super(0, maxTemperature, thermalConductivity, true);
        this.fuel = fuel;
        this.neutronSourceIntensity = neutronSourceIntensity;
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

    public double getNeutronSourceIntensity() {
        return neutronSourceIntensity;
    }

    public FissionFuelProperty getFuel() {
        return fuel;
    }

}
