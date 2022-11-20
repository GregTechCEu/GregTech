package gregtech.api.advancement;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IAdvancementTrigger<T extends IAdvancementCriterion> extends ICriterionTrigger<T> {

    void trigger(EntityPlayerMP player);
}
