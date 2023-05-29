package gregtech.api.unification.material.properties;

public class FissionFuelProperty implements IMaterialProperty{

    private double[] crossSections = new double[]{0.D, 0.D};
    private double durability = 0.D;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

    public void setCrossSections(double slowCrossSection, double fastCrossSection) {
        this.crossSections[0] = slowCrossSection;
        this.crossSections[1] = fastCrossSection;
    }

    public double[] getCrossSections() {
        return this.crossSections;
    }

    public void setDurability(double durability) {
        this.durability = durability;
    }

    public double getDurability() {
        return this.durability;
    }

}
