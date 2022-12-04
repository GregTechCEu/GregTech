package gregtech.api.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISoundManager {

    /**
     * Register a Sound.
     *
     * Must be registered in the {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent} phase.
     *
     * @param soundName The name of the sound in the resources directory.
     * @return The created SoundEvent.
     */
    SoundEvent registerSound(String modName, String soundName);

    /**
     * Register a Sound.
     *
     * Defaults to using the current active module container's ID.
     *
     * Must be registered in the {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent} phase.
     *
     * @param soundName The name of the sound in the resources directory.
     * @return The created SoundEvent.
     */
    SoundEvent registerSound(String soundName);

    /**
     * Starts a positioned sound at a provided BlockPos.
     *
     * @param soundName The name of the sound to play.
     * @param volume    The volume multiplier of the sound.
     * @param pos       The position to play the sound at.
     * @return The sound that was played.
     */
    @SideOnly(Side.CLIENT)
    ISound startTileSound(ResourceLocation soundName, float volume, BlockPos pos);

    /**
     * Stops the positioned sound playing at a given BlockPos (if any).
     */
    @SideOnly(Side.CLIENT)
    void stopTileSound(BlockPos pos);
}
