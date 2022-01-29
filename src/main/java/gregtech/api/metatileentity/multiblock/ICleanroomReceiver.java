package gregtech.api.metatileentity.multiblock;

public interface ICleanroomReceiver {

    ICleanroomProvider getCleanroom();

    void setCleanroom(ICleanroomProvider provider);
}
