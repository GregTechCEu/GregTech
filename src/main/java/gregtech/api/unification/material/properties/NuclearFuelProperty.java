package gregtech.api.unification.material.properties;

public class NuclearFuelProperty implements IMaterialProperty {
    private int maxTemperature;
    private int duration;
    private double thermalNeutronCrossSection;
    private double fastNeutronCrossSection;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }


}
