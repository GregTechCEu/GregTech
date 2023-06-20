package gregtech.api.capability;

public interface ILockableHandler {
    void lock();
    void unlock();
    boolean isLocked();
}
