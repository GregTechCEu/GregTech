package gregtech.api.metatileentity.multiblock;

public interface ICleanroomProvider {

    CleanroomType getType();

    void setClean(boolean isClean);

    boolean isClean();

    boolean drainEnergy(boolean simulate);

    long getEnergyInputPerSecond();

    int getEnergyTier();
}
