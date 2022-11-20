package gregtech.core.advancement.internal;

import gregtech.api.GregTechAPI;
import gregtech.api.advancement.IAdvancementCriterion;
import gregtech.api.advancement.IAdvancementManager;
import gregtech.api.advancement.IAdvancementTrigger;
import gregtech.api.modules.ModuleStage;
import gregtech.core.CoreModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdvancementManager implements IAdvancementManager {

    private static final AdvancementManager INSTANCE = new AdvancementManager();

    private final List<IAdvancementTrigger<?>> triggers;

    private AdvancementManager() {
        this.triggers = new ArrayList<>();
    }

    public static AdvancementManager getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<IAdvancementTrigger<?>> getTriggers() {
        return triggers;
    }

    @Override
    public <T extends IAdvancementCriterion> IAdvancementTrigger<T> registerTrigger(String id, T criterion) {
        if (GregTechAPI.moduleManager.hasPassedStage(ModuleStage.PRE_INIT)) {
            CoreModule.logger.error("Could not register advancement trigger {}, as trigger registration has ended!", id);
            return null;
        }
        IAdvancementTrigger<T> trigger = new AdvancementTrigger<>(id, criterion);
        criterion.setId(trigger.getId());
        triggers.add(trigger);
        return trigger;
    }
}
