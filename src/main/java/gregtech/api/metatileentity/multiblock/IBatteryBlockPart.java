package gregtech.api.metatileentity.multiblock;

import javax.annotation.Nonnull;

public interface IBatteryBlockPart {

    int getTier();

    long getCapacity();

    @Nonnull String getName();
}
