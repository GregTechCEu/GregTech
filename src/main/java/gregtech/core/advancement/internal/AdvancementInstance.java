package gregtech.core.advancement.internal;

import gregtech.api.advancement.IAdvancementInstance;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public abstract class AdvancementInstance extends AbstractCriterionInstance implements IAdvancementInstance {

    public AdvancementInstance(ResourceLocation id) {
        super(id);
    }
}
