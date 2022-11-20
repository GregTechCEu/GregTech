package gregtech.api.advancement;

import java.util.Collection;

public interface IAdvancementManager {

    Collection<IAdvancementTrigger<?>> getTriggers();

    <T extends IAdvancementCriterion> IAdvancementTrigger<T> registerTrigger(String id, T criterion);
}
