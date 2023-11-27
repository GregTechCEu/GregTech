package gregtech.api.unification.material.event;

import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MaterialRegistry;

import net.minecraftforge.fml.common.eventhandler.GenericEvent;

/**
 * Event to add a material registry in.
 *
 * @see IMaterialRegistryManager#createRegistry(String)
 */
public class MaterialRegistryEvent extends GenericEvent<MaterialRegistry> {

    public MaterialRegistryEvent() {
        super(MaterialRegistry.class);
    }
}
