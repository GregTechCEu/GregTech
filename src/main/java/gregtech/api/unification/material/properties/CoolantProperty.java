package gregtech.api.unification.material.properties;

public class CoolantProperty implements IMaterialProperty<CoolantProperty>{

    private double moderatorFactor = 0.D;
    private double coolingFactor = 0.D;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public void setModeratorFactor(double moderatorFactor) {
        this.moderatorFactor = moderatorFactor;
    }

    public double getModeratorFactor() {
        return this.moderatorFactor;
    }

    public void setCoolingFactor(double coolingFactor) {
        this.coolingFactor = coolingFactor;
    }

    public double getCoolingFactor() {
        return this.coolingFactor;
    }
}
