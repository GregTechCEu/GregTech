package gregtech.core.advancement.triggers;

import gregtech.core.advancement.internal.AdvancementInstance;
import gregtech.core.advancement.internal.AdvancementTrigger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Use this class if a Trigger should fail unless the player is killed.
 */
public class TriggerDeath extends AdvancementTrigger<TriggerDeath.Instance> {

    public TriggerDeath(String id) {
        super(id);
    }

    @Override
    public Instance create() {
        return new TriggerDeath.Instance(getId());
    }

    protected static class Instance extends AdvancementInstance {

        public Instance(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean test(EntityPlayerMP player) {
            return !player.isEntityAlive();
        }
    }
}
