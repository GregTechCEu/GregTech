package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.IFluidTank;

public class NotifiableFluidTankList extends FluidTankList implements INotifiableHandler {

    MetaTileEntity notifiableEntity;
    private final boolean isExport;

    public NotifiableFluidTankList(boolean allowSameFluidFill, MetaTileEntity entityToNotify, boolean isExport, IFluidTank... fluidTanks) {
        super(allowSameFluidFill, fluidTanks);
        this.notifiableEntity = entityToNotify;
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        addToNotifiedList(notifiableEntity, this, isExport);
    }

    @Override
    public void setNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntity = metaTileEntity;
    }
}
