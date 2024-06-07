package gregtech.api.capability;

public interface ILockableHandler<T> {

    void setLock(boolean isLocked);

    boolean isLocked();

    T getLockedObject();
}
