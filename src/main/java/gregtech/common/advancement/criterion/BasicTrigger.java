package gregtech.common.advancement.criterion;

import gregtech.api.util.advancement.GTInstance;
import gregtech.api.util.advancement.GTTrigger;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Use this class if a Trigger should always pass when called.
 */
public class BasicTrigger extends GTTrigger<BasicTrigger.BasicInstance> {

    public BasicTrigger(String id) {
        super(id);
    }

    @Override
    public BasicInstance create() {
        return new BasicInstance(getId());
    }

    protected static class BasicInstance extends GTInstance {

        public BasicInstance(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean test(EntityPlayerMP player) {
            return true;
        }
    }
}
