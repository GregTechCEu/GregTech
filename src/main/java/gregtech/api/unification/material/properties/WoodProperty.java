package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.pipelike.handlers.properties.MaterialFluidProperties;

public class WoodProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST);
        properties.getMaterial().addFlags(MaterialFlags.FLAMMABLE);
    }
}
