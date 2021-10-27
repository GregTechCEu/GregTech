package gregtech.api.multiblock;

public interface IMaintenance {

    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();
}
