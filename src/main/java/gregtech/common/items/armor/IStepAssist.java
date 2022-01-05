package gregtech.common.items.armor;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;

/**
 * Logic from EnderIO: https://github.com/SleepyTrousers/EnderIO/blob/d6dfb9d3964946ceb9fd72a66a3cff197a51a1fe/enderio-base/src/main/java/crazypants/enderio/base/handler/darksteel/DarkSteelController.java
 */
public interface IStepAssist {

    float MAGIC_STEP_HEIGHT = 1.0023f;

    default void updateStepHeight(@Nonnull EntityPlayer player) {
        if (!player.isSneaking()) {
            if (player.stepHeight < MAGIC_STEP_HEIGHT) {
                player.stepHeight = MAGIC_STEP_HEIGHT;
            }
        } else if (player.stepHeight == MAGIC_STEP_HEIGHT) {
            player.stepHeight = 0.6F;
        }
    }
}
