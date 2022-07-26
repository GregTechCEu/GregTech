package gregtech.api.nuclear.components;

public class Coolant {

    int pressure;
    int moderatorFactor;
    int absorptionFactor;
    int boilingPoint;
    int temperature;

    Coolant(int pressure, int moderatorFactor, int absorptionFactor, int boilingPoint, int temperature) {
        this.pressure = pressure;
        this.moderatorFactor = moderatorFactor;
        this.absorptionFactor = absorptionFactor;
        this.boilingPoint = boilingPoint;
        this.temperature = temperature;
    }

    public int getPressure() {
        return pressure;
    }

    public int getModeratorFactor() {
        return moderatorFactor;
    }

    public int getAbsorptionFactor() {
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
