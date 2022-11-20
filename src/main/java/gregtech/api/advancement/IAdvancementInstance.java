package gregtech.api.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IAdvancementInstance extends ICriterionInstance {

    boolean test(EntityPlayerMP player);
}
