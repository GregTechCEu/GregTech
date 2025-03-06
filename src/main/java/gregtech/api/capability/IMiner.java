package gregtech.api.capability;

import gregtech.api.recipes.lookup.property.PropertySet;

import codechicken.lib.vec.Cuboid6;
import org.jetbrains.annotations.NotNull;

public interface IMiner {

    Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    @NotNull
    PropertySet computePropertySet();

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
