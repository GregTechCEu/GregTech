package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.FluidTank;

public class NotifiableFluidTank extends FluidTank implements INotifiableHandler {

    MetaTileEntity notifiableEntity;
    private final boolean isExport;

    public NotifiableFluidTank(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity);
        this.notifiableEntity = entityToNotify;
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        notifyMetaTileEntityOfChange(notifiableEntity, isExport);
    }

    @Override
    public void setNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntity = metaTileEntity;
    }
}
