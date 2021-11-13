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

    /**
     * DO NOT USE THIS FOR ANY MTEs THAT APPEAR IN A JEI CATEGORY PAGE
     * @param sound The sound that this creator emits when running.
     * @param pos The position of this creator in the world.
     */
    default void setupSound(SoundEvent sound, BlockPos pos) {
        if (sound != null && ConfigHolder.machineSounds) {
            PositionedSoundMTE machineSound = new PositionedSoundMTE(sound.getSoundName(), SoundCategory.BLOCKS, this, pos);
            Minecraft.getMinecraft().getSoundHandler().playSound(machineSound);
            Minecraft.getMinecraft().getSoundHandler().update();
        }
    }

    default void setupSound(SoundEvent sound, BlockPos pos, World world) {
        if (sound != null && ConfigHolder.machineSounds && world != null) {
            PositionedSoundMTE machineSound = new PositionedSoundMTE(sound.getSoundName(), SoundCategory.BLOCKS, this, pos);
            Minecraft.getMinecraft().getSoundHandler().playSound(machineSound);
            Minecraft.getMinecraft().getSoundHandler().update();
        }
    }

    // TODO: When miners are released, please make them implement this and use the GTSounds.MINER sound effect.
}
