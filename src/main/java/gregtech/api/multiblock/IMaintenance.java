package gregtech.api.multiblock;

public interface IMaintenance {

    byte getProblems();

    int getNumProblems();

    boolean hasProblems();

    void setMaintenanceFixed(int index);

    void storeTaped(boolean isTaped);
}
