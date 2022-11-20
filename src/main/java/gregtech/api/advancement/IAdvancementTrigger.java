package gregtech.api.advancement;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IAdvancementTrigger<T extends IAdvancementInstance> extends ICriterionTrigger<T> {

    T create();

    void trigger(EntityPlayerMP player);
}
