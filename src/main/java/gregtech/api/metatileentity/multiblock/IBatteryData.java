package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;

public interface IBatteryData {

    int getTier();

    long getCapacity();

    @NotNull
    String getBatteryName();
}
