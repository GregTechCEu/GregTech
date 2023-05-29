package gregtech.api.unification.material.event;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistryManager;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

import javax.annotation.Nonnull;

/**
 * Event to register and modify materials in
 */
public class MaterialEvent extends GenericEvent<Material> {

    public MaterialEvent() {
        super(Material.class);
    }

    /**
     * Begins material registration for the current mod.
     * <p>
     * <strong>Every mod should call this before registering materials.</strong>
     *
     * @param modid the modid performing material registration
     */
    @SuppressWarnings("MethodMayBeStatic")
    public void startRegistration(@Nonnull String modid) {
        Material.Builder.setConstructionRegistry(MaterialRegistryManager.getRegistry(modid));
    }

    /**
     * Completes material registration for the current mod.
     * <p>
     * <strong>Every mod should call this after registering materials.</strong>
     */
    @SuppressWarnings("methodMayBeStatic")
    public void completeRegistration() {
        Material.Builder.setConstructionRegistry(MaterialRegistryManager.getRegistry(GTValues.MODID));
    }
}
