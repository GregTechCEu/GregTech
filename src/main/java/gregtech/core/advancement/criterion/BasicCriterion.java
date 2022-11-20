package gregtech.core.advancement.criterion;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Will always succeed when a Trigger with this Criterion is fired.
 */
public class BasicCriterion extends AbstractCriterion {

    @Override
    public boolean test(EntityPlayerMP player) {
        return true;
    }
}
