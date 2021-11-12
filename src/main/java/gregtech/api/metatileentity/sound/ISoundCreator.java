package gregtech.api.metatileentity.sound;

public interface ISoundCreator {
    boolean canCreateSound();

    boolean isValid();

    // TODO: When miners are released, please make them implement this and use the GTSounds.MINER sound effect.
}
