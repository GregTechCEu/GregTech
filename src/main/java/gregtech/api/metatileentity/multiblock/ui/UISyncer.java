package gregtech.api.metatileentity.multiblock.ui;

public interface UISyncer {
    boolean syncBoolean(boolean initial);
    int syncInt(int initial);
    long syncLong(long initial);
    String syncString(String initial);
    byte syncByte(byte initial);
    double syncDouble(double initial);
    float syncFloat(float initial);
}
