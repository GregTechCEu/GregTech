package gregtech.api.capability.impl;

import gregtech.api.capability.ILockableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class LockableFluidTank extends NotifiableFluidTank implements ILockableHandler {

    boolean locked;
    Fluid lockedFluid;

    public LockableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity, entityToNotify, isExport);
    }

    @Override
    public void setLock(boolean isLocked) {
        locked = isLocked;
        if (isLocked) {
            if (this.getFluid() == null)
                lockedFluid = null;
            else
                lockedFluid = this.getFluid().getFluid();
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        if (locked && fluid.getFluid() != lockedFluid) {
            return false;
        }
        return super.canFillFluidType(fluid);
    }
}
