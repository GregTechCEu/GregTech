package gregtech.api.unification.material;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;

import org.jetbrains.annotations.NotNull;

/**
 * MarkerMaterial is type of material used for generic things like material re-registration and use in recipes
 * Marker material cannot be used to generate any meta items
 * Marker material can be used only for marking other materials (re-registering) equal to it and then using it in
 * recipes or in getting items
 * Marker material is not presented in material registry and cannot be used for persistence
 */
public final class MarkerMaterial extends Material {

    private MarkerMaterial(@NotNull String name) {
        super(GTUtility.gregtechId(name));
    }

    /**
     * Create a new MarkerMaterial
     *
     * @param name the name of the MarkerMaterial
     * @return the new MarkerMaterial
     */
    public static @NotNull MarkerMaterial create(@NotNull String name) {
        MarkerMaterial markerMaterial = new MarkerMaterial(name);
        return GregTechAPI.markerMaterialRegistry.registerMarkerMaterial(markerMaterial);
    }

    @Override
    protected void registerMaterial() {}

    @Override
    public void verifyMaterial() {}
}
