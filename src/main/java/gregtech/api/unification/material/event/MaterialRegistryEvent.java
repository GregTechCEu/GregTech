package gregtech.api.unification.material.event;

import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.material.registry.MaterialRegistryManager;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

/**
 * Event to add a material registry in.
 *
 * @see MaterialRegistryManager#createRegistry(String)
 */
public class MaterialRegistryEvent extends GenericEvent<MaterialRegistry> {

    public MaterialRegistryEvent() {
        super(MaterialRegistry.class);
    }
}
