package gregtech.api.capability;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface ILockableItemHandler extends ILockableHandler, IItemHandlerModifiable {
    void lock();
    void unlock();
    boolean isLocked();
}
