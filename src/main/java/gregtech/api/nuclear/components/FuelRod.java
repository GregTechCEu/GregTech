package gregtech.api.nuclear.components;

import gregtech.api.nuclear.NuclearFuel;
import gregtech.api.nuclear.ReactorComponent;

public class FuelRod extends ReactorComponent {
    private final NuclearFuel fuel;
    private final float neutronSourceIntensity;

    public FuelRod(int maxTemperature, double thermalConductivity, NuclearFuel fuel, float neutronSourceIntensity) {
        super(true, maxTemperature, 0, thermalConductivity);
        this.fuel = fuel;
        this.neutronSourceIntensity = neutronSourceIntensity;
    }

    double getHEFissionFactor() {
        return fuel.getCs_vector()[0];
    }

    double getLEFissionFactor() {
        return fuel.getCs_vector()[1];
    }

    double getHECaptureFactor() {
        return fuel.getCs_vector()[2];
    }

    double getLECaptureFactor() {
        return fuel.getCs_vector()[3];
    }

    public float getNeutronSourceIntensity() {
        return neutronSourceIntensity;
    }

    public NuclearFuel getFuel() {
        return fuel;
    }

    @Override
    public FuelRod copy() {
        return new FuelRod(getMaxTemperature(), getThermalConductivity(), fuel, neutronSourceIntensity);
    }
}
