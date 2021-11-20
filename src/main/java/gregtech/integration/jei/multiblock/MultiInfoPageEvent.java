package gregtech.integration.jei.multiblock;

import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Map;

public class MultiInfoPageEvent extends Event {

    private final Map<String, MultiblockInfoRecipeWrapper> registry;

    public MultiInfoPageEvent(Map<String, MultiblockInfoRecipeWrapper> registry) {
        this.registry = registry;
    }

    public void register(String id, MultiblockInfoRecipeWrapper infoPage) {
        if (registry.containsKey(id)) {
            GTLog.logger.error("Duplicate info page name found ({}), skipping...", id);
        } else this.registry.put(id, infoPage);
    }

    public void remove(String id) {
        if (!registry.containsKey(id)) {
            GTLog.logger.error("Could not find info page with name {} to remove, skipping...", id);
        } else this.registry.remove(id);
    }
}
