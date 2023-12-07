package gregtech.api.unification.material.event;

import gregtech.api.unification.material.Material;

import net.minecraftforge.fml.common.eventhandler.GenericEvent;

/**
 * Event to modify and perform post-processing on materials
 */
public class PostMaterialEvent extends GenericEvent<Material> {

    public PostMaterialEvent() {
        super(Material.class);
    }
}
