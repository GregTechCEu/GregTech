package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.pipelike.handlers.properties.MaterialFluidProperties;

public class WoodProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST);
        properties.getMaterial().addFlags(MaterialFlags.FLAMMABLE);

        PipeNetProperties netProperties = properties.getProperty(PropertyKey.PIPENET_PROPERTIES);
        if (netProperties != null && netProperties.hasProperty(MaterialFluidProperties.KEY)) {
            OrePrefix.pipeTiny.setIgnored(properties.getMaterial());
            OrePrefix.pipeHuge.setIgnored(properties.getMaterial());
            OrePrefix.pipeQuadruple.setIgnored(properties.getMaterial());
            OrePrefix.pipeNonuple.setIgnored(properties.getMaterial());
        }
    }
}
