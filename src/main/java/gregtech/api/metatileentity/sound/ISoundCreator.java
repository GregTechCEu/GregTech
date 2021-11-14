package gregtech.api.metatileentity.sound;

import gregtech.common.sound.SoundHandler;
import gregtech.common.ConfigHolder;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISoundCreator {
    boolean canCreateSound();

    boolean isValid();

    /**
     * @param sound The sound that this creator emits when running.
     * @param pos The position of this creator in the world.
     */

    default void setupSound(SoundEvent sound, BlockPos pos, World world) {
        if (sound != null && ConfigHolder.machineSounds && world != null && world.isRemote) {
            SoundHandler.startTileSound(sound.getSoundName(), pos, this, 1, 0);
        }
    }

    // TODO: When miners are released, please make them implement this and use the GTSounds.MINER sound effect.
}
