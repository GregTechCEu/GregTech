package gregtech.core.sound;

import net.minecraft.util.SoundEvent;

import static gregtech.api.GregTechAPI.soundManager;

public class GTSoundEvents {

    // Record Sounds
    public static SoundEvent SUS_RECORD;

    // Entity Sounds
    public static SoundEvent PORTAL_OPENING;
    public static SoundEvent PORTAL_CLOSING;

    public static void register() {
        SUS_RECORD = soundManager.registerSound("record.sus");
        PORTAL_OPENING = soundManager.registerSound("entity.portal_opening");
        PORTAL_CLOSING = soundManager.registerSound("entity.portal_closing");
    }
}
