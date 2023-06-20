package gregtech.api.capability.impl;

import gregtech.api.capability.ILockableTank;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class LockableFluidTank extends NotifiableFluidTank implements ILockableTank {
    boolean locked;
    Fluid lockedFluid;

    public LockableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity, entityToNotify, isExport);
    }


    @Override
    public void lock() {
        locked = true;
        lockedFluid = this.getFluid().getFluid();
    }

    @Override
    public void unlock() {
        locked = false;
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
