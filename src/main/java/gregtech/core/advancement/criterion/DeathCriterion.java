package gregtech.core.advancement.criterion;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Will succeed if the tested player is dead when a Trigger with this Criterion is fired.
 */
public class DeathCriterion extends AbstractCriterion {

    @Override
    public boolean test(EntityPlayerMP player) {
        return !player.isEntityAlive();
    }
}
