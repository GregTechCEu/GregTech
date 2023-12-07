package gregtech.api.capability;

public interface ILockableHandler {

    void setLock(boolean isLocked);

    boolean isLocked();
}
