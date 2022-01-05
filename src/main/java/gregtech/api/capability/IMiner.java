package gregtech.api.capability;


import codechicken.lib.vec.Cuboid6;

public interface IMiner {

    Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    boolean drainEnergy(boolean simulate);

    default boolean drainFluid(boolean simulate) {
        return true;
    }

    boolean isInventoryFull();

    void setInventoryFull(boolean isFull);

    default int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
