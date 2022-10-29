package gregtech.api.metatileentity.multiblock;

public interface IMaintenance {

    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void causeMaintenanceProblems();

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();
}
