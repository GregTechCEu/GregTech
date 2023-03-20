package gregtech.api.advancement;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public interface IAdvancementCriterion extends ICriterionInstance {

    boolean test(EntityPlayerMP player);

    void setId(ResourceLocation id);
}
