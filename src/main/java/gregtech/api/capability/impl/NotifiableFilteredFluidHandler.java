package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import java.util.LinkedList;

public class NotifiableFilteredFluidHandler extends FilteredFluidHandler implements INotifiableHandler {

    LinkedList<MetaTileEntity> entitiesToNotify = new LinkedList<>();
    private final boolean isExport;

    public NotifiableFilteredFluidHandler(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
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
