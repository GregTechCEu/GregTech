package gregtech.api.capability;

import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.unification.material.Material;

public interface ICoolantHandler extends ILockableHandler{

    Material getCoolant();

    void setCoolant(Material material);

    LockableFluidTank getFluidTank();

}
