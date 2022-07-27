package gregtech.api.unification.material.properties;

public class CoolingProperty implements IMaterialProperty<CoolingProperty> {
    int pressure;
    int moderatorFactor;
    double absorptionFactor;
    int boilingPoint;
    int temperature;
    public CoolingProperty(int pressure, int moderatorFactor, double absorptionFactor, int boilingPoint, int temperature) {
        this.pressure = pressure;
        this.moderatorFactor = moderatorFactor;
        this.absorptionFactor = absorptionFactor;
        this.boilingPoint = boilingPoint;
        this.temperature = temperature;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public int getPressure() {
        return pressure;
    }

    public int getModeratorFactor() {
        return moderatorFactor;
    }

    public double getAbsorptionFactor() {
        return absorptionFactor;
    }

    public int getBoilingPoint() {
        return boilingPoint;
    }

    public int getTemperature() {
        return temperature;
    }

    public int coolantTemperatureFactor(int temperature, int temperature_boil, int a, int m, int p) {
        if (temperature > 3 * temperature_boil) {
            return (temperature - 3 * temperature_boil) * (a - m) / (3 * p) + m - a;
        } else {
            return m - a;
        }
    }
}
