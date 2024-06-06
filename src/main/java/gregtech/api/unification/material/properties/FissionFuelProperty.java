package gregtech.api.unification.material.properties;

public class FissionFuelProperty implements IMaterialProperty {
    // The max temperature the fuel can handle before it liquefies.
    private int maxTemperature;
    // Scales how long the fuel rod lasts in the reactor.
    private int duration;
    // How likely it is to absorb a neutron that had touched a moderator.
    private double slowNeutronCaptureCrossSection;
    // How likely it is to absorb a neutron that has not yet touched a moderator.
    private double fastNeutronCaptureCrossSection;
    // How likely it is for a moderated neutron to cause fission in this fuel.
    private double slowNeutronFissionCrossSection;
    // How likely it is for a not-yet-moderated neutron to cause fission in this fuel.
    private double fastNeutronFissionCrossSection;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    public FissionFuelProperty(int maxTemperature, int duration, double slowNeutronCaptureCrossSection,
                               double fastNeutronCaptureCrossSection, double slowNeutronFissionCrossSection,
                               double fastNeutronFissionCrossSection) {
        this.maxTemperature = maxTemperature;
        this.duration = duration;
        this.slowNeutronCaptureCrossSection = slowNeutronCaptureCrossSection;
        this.fastNeutronCaptureCrossSection = fastNeutronCaptureCrossSection;
        this.slowNeutronFissionCrossSection = slowNeutronFissionCrossSection;
        this.fastNeutronFissionCrossSection = fastNeutronFissionCrossSection;
    }

    public FissionFuelProperty() {
        this(1, 1, 0.1D, 0.1D, 0.1D, 0.1D);
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        if (maxTemperature <= 0) throw new IllegalArgumentException("Max temperature must be greater than zero!");
        this.maxTemperature = maxTemperature;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration <= 0) throw new IllegalArgumentException("Fuel duration must be greater than zero!");
        this.duration = duration;
    }

    public double getSlowNeutronCaptureCrossSection() {
        return slowNeutronCaptureCrossSection;
    }

    public void setSlowNeutronCaptureCrossSection(double slowNeutronCaptureCrossSection) {
        this.slowNeutronCaptureCrossSection = slowNeutronCaptureCrossSection;
    }

    public double getFastNeutronCaptureCrossSection() {
        return fastNeutronCaptureCrossSection;
    }

    public void setFastNeutronCaptureCrossSection(double fastNeutronCaptureCrossSection) {
        this.fastNeutronCaptureCrossSection = fastNeutronCaptureCrossSection;
    }

    public double getSlowNeutronFissionCrossSection() {
        return slowNeutronFissionCrossSection;
    }

    public void setSlowNeutronFissionCrossSection(double slowNeutronFissionCrossSection) {
        this.slowNeutronFissionCrossSection = slowNeutronFissionCrossSection;
    }

    public double getFastNeutronFissionCrossSection() {
        return fastNeutronFissionCrossSection;
    }

    public void setFastNeutronFissionCrossSection(double fastNeutronFissionCrossSection) {
        this.fastNeutronFissionCrossSection = fastNeutronFissionCrossSection;
    }
}
