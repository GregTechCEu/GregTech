package gregtech.core.advancement.triggers;

import gregtech.core.advancement.internal.AdvancementInstance;
import gregtech.core.advancement.internal.AdvancementTrigger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Use this class if a Trigger should fail unless the player is killed.
 */
public class TriggerDeath extends AdvancementTrigger<TriggerDeath.RotorInstance> {

    public TriggerDeath(String id) {
        super(id);
    }

    @Override
    public RotorInstance create() {
        return new TriggerDeath.RotorInstance(getId());
    }

    protected static class RotorInstance extends AdvancementInstance {

        public RotorInstance(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean test(EntityPlayerMP player) {
            return !player.isEntityAlive();
        }
    }
}
