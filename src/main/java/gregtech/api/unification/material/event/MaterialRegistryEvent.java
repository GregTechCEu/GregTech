package gregtech.api.unification.material.event;

import gregtech.api.unification.material.registry.MaterialRegistrationManager;
import gregtech.api.unification.material.registry.MaterialRegistry;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

/**
 * Event to add a material registry in.
 *
 * @see MaterialRegistrationManager#createRegistry(String)
 */
public class MaterialRegistryEvent extends GenericEvent<MaterialRegistry> {

    public MaterialRegistryEvent() {
        super(MaterialRegistry.class);
    }
}
