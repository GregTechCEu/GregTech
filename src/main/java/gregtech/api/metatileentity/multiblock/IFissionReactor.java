package gregtech.api.metatileentity.multiblock;

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
