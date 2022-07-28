package gregtech.api.nuclear.components;

import gregtech.api.nuclear.NuclearFuel;
import gregtech.api.nuclear.ReactorComponent;

public class FuelRod extends ReactorComponent {
    private final NuclearFuel fuel;
    private final double neutronSourceIntensity;

    public FuelRod(double maxTemperature, double thermalConductivity, NuclearFuel fuel, double neutronSourceIntensity) {
        super(true, maxTemperature, 0, thermalConductivity);
        this.fuel = fuel;
        this.neutronSourceIntensity = neutronSourceIntensity;
    }

    public double getHEFissionFactor() {
        return fuel.getCs_vector()[0];
    }

    public double getLEFissionFactor() {
        return fuel.getCs_vector()[1];
    }

    public double getHECaptureFactor() {
        return fuel.getCs_vector()[2];
    }

    public double getLECaptureFactor() {
        return fuel.getCs_vector()[3];
    }

    public double getNeutronSourceIntensity() {
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
