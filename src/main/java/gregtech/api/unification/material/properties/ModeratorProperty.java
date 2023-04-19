package gregtech.api.unification.material.properties;

public class ModeratorProperty implements IMaterialProperty<ModeratorProperty>{

    private double moderatorFactor = 0.D;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public void setModeratorFactor(double moderatorFactor) {
        this.moderatorFactor = moderatorFactor;
    }

    public double getModeratorFactor() {
        return this.moderatorFactor;
    }

}
