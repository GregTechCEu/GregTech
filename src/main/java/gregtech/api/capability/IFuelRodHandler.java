package gregtech.api.capability;

import gregtech.api.items.itemhandlers.LockableItemStackHandler;
import gregtech.api.unification.material.Material;

public interface IFuelRodHandler extends ILockableHandler{

    Material getFuel();

    void setFuel(Material material);

    LockableItemStackHandler getStackHandler();

}
