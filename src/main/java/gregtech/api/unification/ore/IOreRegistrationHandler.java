package gregtech.api.unification.ore;

import gregtech.api.unification.material.Material;

@FunctionalInterface
public interface IOreRegistrationHandler {

    void processMaterial(OrePrefix orePrefix, Material material);
}
