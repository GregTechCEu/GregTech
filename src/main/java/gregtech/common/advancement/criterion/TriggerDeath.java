package gregtech.common.advancement.criterion;

import gregtech.api.util.advancement.GTInstance;
import gregtech.api.util.advancement.GTTrigger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Use this class if a Trigger should fail unless the player is killed.
 */
public class TriggerDeath extends GTTrigger<TriggerDeath.RotorInstance> {

    public TriggerDeath(String id) {
        super(id);
    }

    @Override
    public RotorInstance create() {
        return new TriggerDeath.RotorInstance(getId());
    }

    protected static class RotorInstance extends GTInstance {

        public RotorInstance(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean test(EntityPlayerMP player) {
            return !player.isEntityAlive();
        }
    }
}
