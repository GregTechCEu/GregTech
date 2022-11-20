package gregtech.api.advancement;

public interface IAdvancementManager {

    <T extends IAdvancementCriterion> IAdvancementTrigger<T> registerTrigger(String id, T criterion);
}
