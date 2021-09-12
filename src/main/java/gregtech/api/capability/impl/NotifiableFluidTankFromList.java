package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;

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
}
