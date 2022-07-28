package gregtech.api.unification.material.properties;

public class CoolingProperty implements IMaterialProperty<CoolingProperty> {
    double pressure;
    double moderatorFactor;
    double absorptionFactor;
    double boilingPoint;
    double temperature;
    public CoolingProperty(double pressure, double moderatorFactor, double absorptionFactor, double boilingPoint, double temperature) {
        this.pressure = pressure;
        this.moderatorFactor = moderatorFactor;
        this.absorptionFactor = absorptionFactor;
        this.boilingPoint = boilingPoint;
        this.temperature = temperature;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public double getPressure() {
        return pressure;
    }

    public double getModeratorFactor() {
        return moderatorFactor;
    }

    public double getAbsorptionFactor() {
        return absorptionFactor;
    }

    public double getBoilingPoint() {
        return boilingPoint;
    }

    public double getTemperature() {
        return temperature;
    }

    public static double coolantTemperatureFactor(double temperature, double temperature_boil, double absorption, double moderation, double pressure) {
        if (temperature > 3 * temperature_boil) {
            return (temperature - 3 * temperature_boil) * (absorption - moderation) / (3 * pressure) + moderation - absorption;
        } else {
            return moderation - absorption;
        }
    }
}
