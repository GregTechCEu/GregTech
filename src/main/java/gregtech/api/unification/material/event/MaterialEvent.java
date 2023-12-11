package gregtech.api.unification.material.event;

import gregtech.api.unification.material.Material;

import net.minecraftforge.fml.common.eventhandler.GenericEvent;

/**
 * Event to register and modify materials in
 */
public class MaterialEvent extends GenericEvent<Material> {

    public MaterialEvent() {
        super(Material.class);
    }
}
