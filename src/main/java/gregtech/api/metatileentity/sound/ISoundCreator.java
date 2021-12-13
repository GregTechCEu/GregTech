package gregtech.api.metatileentity.sound;

import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @SideOnly(Side.CLIENT)
    default void setupSound(SoundEvent sound, BlockPos pos) {
        if (sound != null && ConfigHolder.machines.machineSounds) {
            PositionedSoundMTE machineSound = new PositionedSoundMTE(sound.getSoundName(), SoundCategory.BLOCKS, this, pos);
            Minecraft.getMinecraft().getSoundHandler().playSound(machineSound);
            Minecraft.getMinecraft().getSoundHandler().update();
        }
    }
}
