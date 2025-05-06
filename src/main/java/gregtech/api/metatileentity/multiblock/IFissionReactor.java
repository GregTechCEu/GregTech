package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IFissionRodPort;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFissionReactor {

    void recomputeRodStats();

    boolean isActive();

    int getBaseTemperature();

    int getTemperature();

    int getInstability();

    int getTemperatureLimit();

    int getFragility();

    double getRateFactor();

    void applyHeat(long heat);

    double getModeratorRodBonus();

    int getControlRodBonus();
}
