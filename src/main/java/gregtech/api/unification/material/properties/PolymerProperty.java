package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.info.MaterialFlags;

public class PolymerProperty implements IMaterialProperty<PolymerProperty>{


    @Override
    public void verifyProperty(MaterialProperties properties) {

        properties.ensureSet(PropertyKey.INGOT, true);
        properties.ensureSet(PropertyKey.FLUID, true);

        properties.getMaterial().addFlags(MaterialFlags.FLAMMABLE, MaterialFlags.NO_SMASHING, MaterialFlags.DISABLE_DECOMPOSITION);

    }
}
