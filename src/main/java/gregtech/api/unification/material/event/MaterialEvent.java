package gregtech.api.unification.material.event;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistrationManager;
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
     * Every mod should call this before registering materials
     *
     * @param modid the modid performing material registration
     */
    @SuppressWarnings("MethodMayBeStatic")
    public void startRegistration(@Nonnull String modid) {
        Material.activeRegistry = MaterialRegistrationManager.getRegistry(modid);
    }
}
