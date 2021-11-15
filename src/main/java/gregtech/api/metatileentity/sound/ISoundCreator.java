package gregtech.api.metatileentity.sound;

import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISoundCreator {
    boolean canCreateSound();

    boolean isValid();

    default boolean isMuffled() {
        return false;
    }

    /**
     * @param sound The sound that this creator emits when running.
     * @param pos The position of this creator in the world.
     */

    default void setupSound(SoundEvent sound, BlockPos pos, World world) {
        if (sound != null && ConfigHolder.machineSounds && world != null && world.isRemote) {
            PositionedSoundMTE machineSound = new PositionedSoundMTE(sound.getSoundName(), SoundCategory.BLOCKS, this, pos);
            Minecraft.getMinecraft().getSoundHandler().playSound(machineSound);
            Minecraft.getMinecraft().getSoundHandler().update();
        }
    }
}
