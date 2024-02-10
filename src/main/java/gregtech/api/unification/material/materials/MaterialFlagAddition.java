package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.unification.material.Materials.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty oreProp = Naquadah.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Barite, NaquadahEnriched);
        oreProp.setSeparatedInto(NaquadahEnriched);
    }
}
