package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

public class MagneticProperty implements IMaterialProperty {

    /**
     * The Material that this Material polarizes into.
     */
    private final Material magneticMaterial;

    /**
     * Property expects that magneticMaterial be properly formed by the Builder upon construction.
     */
    public MagneticProperty(Material magneticMaterial) {
        this.magneticMaterial = magneticMaterial;
    }

    public Material getMagneticMaterial() {
        return magneticMaterial;
    }

    /**
     * Default values constructor.
     */
    public MagneticProperty() {
        this.magneticMaterial = null;
    }

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getIngotProperty() == null) {
            properties.setIngotProperty(new IngotProperty());
            properties.verify();
        }
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof MagneticProperty;
    }

    @Override
    public String getName() {
        return "magnetic_property";
    }
}
