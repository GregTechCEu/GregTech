package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.FluidTank;

import java.util.LinkedList;

public class NotifiableFluidTank extends FluidTank implements INotifiableHandler {

    LinkedList<MetaTileEntity> entitiesToNotify = new LinkedList<>();
    private final boolean isExport;

    public NotifiableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity);
        this.entitiesToNotify.add(entityToNotify);
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        notifyMetaTileEntitiesOfChange(isExport);
    }

    @Override
    public LinkedList<MetaTileEntity> getNotifiableMetaTileEntities() {
        return this.entitiesToNotify;
    }
}
