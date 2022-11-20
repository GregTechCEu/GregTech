package gregtech.core.advancement.triggers;

import gregtech.core.advancement.internal.AdvancementInstance;
import gregtech.core.advancement.internal.AdvancementTrigger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Use this class if a Trigger should always pass when called.
 */
public class BasicTrigger extends AdvancementTrigger<BasicTrigger.BasicInstance> {

    public BasicTrigger(String id) {
        super(id);
    }

    @Override
    public BasicInstance create() {
        return new BasicInstance(getId());
    }

    protected static class BasicInstance extends AdvancementInstance {

        public BasicInstance(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean test(EntityPlayerMP player) {
            return true;
        }
    }
}
