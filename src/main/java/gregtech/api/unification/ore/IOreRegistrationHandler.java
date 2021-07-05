package gregtech.api.unification.ore;

import gregtech.api.unification.material.IMaterial;

@FunctionalInterface
public interface IOreRegistrationHandler {

    void processMaterial(OrePrefix orePrefix, IMaterial<?> material);

}
