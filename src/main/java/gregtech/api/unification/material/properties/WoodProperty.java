package gregtech.api.unification.material.properties;

import gregtech.api.unification.ore.OrePrefix;

public class WoodProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST);

        if (properties.hasProperty(PropertyKey.FLUID_PIPE)) {
            OrePrefix.pipeTinyFluid.setIgnored(properties.getMaterial());
            OrePrefix.pipeHugeFluid.setIgnored(properties.getMaterial());
            OrePrefix.pipeQuadrupleFluid.setIgnored(properties.getMaterial());
            OrePrefix.pipeNonupleFluid.setIgnored(properties.getMaterial());
        }
    }
}
