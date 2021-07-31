package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import javax.annotation.Nullable;

//@ZenClass("mods.gregtech.material.IngotMaterial")
//@ZenRegister
public class IngotProperty implements IMaterialProperty {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    private Material smeltInto;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    private Material arcSmeltInto; // ingot required

    /**
     * Material which obtained when this material is polarized
     */
    @Nullable
    private Material magneticMaterial;

    public void setSmeltingInto(Material smeltInto) {
        this.smeltInto = smeltInto;
    }

    public void setArcSmeltingInto(Material arcSmeltingInto) {
        this.arcSmeltInto = arcSmeltingInto;
    }

    public Material getArcSmeltInto() {
        return this.arcSmeltInto;
    }

    public void setMagneticMaterial(@Nullable Material magneticMaterial) {
        this.magneticMaterial = magneticMaterial;
    }

    public Material getMagneticMaterial() {
        return magneticMaterial;
    }

    @Override
    public void verifyProperty(Properties properties) {
        if (properties.getDustProperty() == null) {
            properties.setDustProperty(new DustProperty());
            properties.verify();
        }

        if (smeltInto == null) smeltInto = properties.getMaterial();
        else {
            Properties smeltIntoProperties = smeltInto.getProperties();
            if (smeltIntoProperties.getIngotProperty() == null) {
                smeltIntoProperties.setIngotProperty(new IngotProperty());
                smeltIntoProperties.verify();
            }
        }

        if (arcSmeltInto == null) arcSmeltInto = properties.getMaterial();
        else {
            Properties arcSmeltIntoProperties = arcSmeltInto.getProperties();
            if (arcSmeltIntoProperties.getIngotProperty() == null) {
                arcSmeltIntoProperties.setIngotProperty(new IngotProperty());
                arcSmeltIntoProperties.verify();
            }
        }

        if (magneticMaterial != null) {
            Properties magneticProperties = magneticMaterial.getProperties();
            if (magneticProperties.getIngotProperty() == null) {
                magneticProperties.setIngotProperty(new IngotProperty());
                magneticProperties.verify();
            }
        }
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof IngotProperty;
    }

    @Override
    public String getName() {
        return "ingot_property";
    }

    @Override
    public String toString() {
        return getName();
    }
}
