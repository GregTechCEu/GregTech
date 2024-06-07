package gregtech.api.capability;

import gregtech.api.capability.impl.LockableFluidTank;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.Fluid;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    Material getCoolant();

    void setCoolant(Material material);

    LockableFluidTank getFluidTank();
}
