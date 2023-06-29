package gregtech.api.unification.material;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;

import javax.annotation.Nonnull;

/**
 * MarkerMaterial is type of material used for generic things like material re-registration and use in recipes
 * Marker material cannot be used to generate any meta items
 * Marker material can be used only for marking other materials (re-registering) equal to it and then using it in recipes or in getting items
 * Marker material is not presented in material registry and cannot be used for persistence
 */
public final class MarkerMaterial extends Material {

    public MarkerMaterial(@Nonnull String name) {
        super(GTUtility.gregtechId(name));
        OreDictUnifier.registerMarkerMaterial(this);
    }

    @Override
    protected void registerMaterial() {
    }

    @Override
    public void verifyMaterial() {
    }
}
