package gregtech.api.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class HighTierEvent extends Event {

    private boolean isHighTier = false;

    /**
     * @return if High-Tier will be enabled by this event.
     */
    public boolean isHighTier() {
        return this.isHighTier;
    }

    /**
     * Used to enable High-Tier. This overrides the config for High-Tier.
     * <p>
     * High-Tier cannot be disabled, once enabled through this event.
     */
    @SuppressWarnings("unused")
    public void enableHighTier() {
        this.isHighTier = true;
    }
}
