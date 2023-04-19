package gregtech.api.nuclear.fission.components;

import gregtech.api.nuclear.fission.fuels.NuclearFuel;

public class FuelRod extends ReactorComponent {
    private final NuclearFuel fuel;
    private final double neutronSourceIntensity;

    public FuelRod(double maxTemperature, double thermalConductivity, NuclearFuel fuel, double neutronSourceIntensity) {
        super(0, maxTemperature, thermalConductivity, true);
        this.fuel = fuel;
        this.neutronSourceIntensity = neutronSourceIntensity;
    }

    public double getHEFissionFactor() {
        return fuel.getCrossSectionVector()[0];
    }

    public double getLEFissionFactor() {
        return fuel.getCrossSectionVector()[1];
    }

    public double getHECaptureFactor() {
        return fuel.getCrossSectionVector()[2];
    }

    public double getLECaptureFactor() {
        return fuel.getCrossSectionVector()[3];
    }

    public double getNeutronSourceIntensity() {
        return neutronSourceIntensity;
    }

    public NuclearFuel getFuel() {
        return fuel;
    }

}
