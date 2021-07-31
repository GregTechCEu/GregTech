package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.Material;

import javax.annotation.Nullable;

//@ZenClass("mods.gregtech.material.IngotMaterial")
//@ZenRegister
// todo extends SolidMaterial
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

    // todo move
    public long verifyMaterialBits(long generationBits) {
        if ((generationBits & GENERATE_DENSE) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_ROTOR) > 0) {
            generationBits |= GENERATE_BOLT_SCREW;
            generationBits |= GENERATE_RING;
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_SMALL_GEAR) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_FINE_WIRE) > 0) {
            generationBits |= GENERATE_FOIL;
        }
        if ((generationBits & GENERATE_FOIL) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_RING) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_BOLT_SCREW) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_FRAME) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_DOUBLE_PLATE) > 0) {
            generationBits |= GENERATE_PLATE;
        }

        return super.verifyMaterialBits(generationBits);
    }

    public void setSmeltingInto(Material smeltInto) {
        this.smeltInto = smeltInto;
    }

    public void setArcSmeltingInto(Material arcSmeltingInto) {
        this.arcSmeltInto = arcSmeltingInto;
    }

    public void setMagneticMaterial(@Nullable Material magneticMaterial) {
        this.magneticMaterial = magneticMaterial;
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
}
