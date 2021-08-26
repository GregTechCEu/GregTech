package gregtech.api.util.advancement;

import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public abstract class GTInstance extends AbstractCriterionInstance {

    public GTInstance(ResourceLocation id) {
        super(id);
    }

    public abstract boolean test(EntityPlayerMP player);
}
