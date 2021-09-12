package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Supplier;

public abstract class NotifiableFluidTankFromList extends NotifiableFluidTank {

    private final int index;

    public NotifiableFluidTankFromList(int capacity, MetaTileEntity entityToNotify, boolean isExport, int index) {
        super(capacity, entityToNotify, isExport);
        this.index = index;
    }

    public abstract Supplier<IMultipleTankHandler> getFluidTankList();

    public int getIndex() {
        return index;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        IMultipleTankHandler tanks = getFluidTankList().get();
        if (!tanks.allowSameFluidFill()) {
            int fillIndex = tanks.getIndexOfFluid(resource);
            if (fillIndex != getIndex() && fillIndex != -1) return 0;
        }
        return super.fill(resource, doFill);
    }
}
