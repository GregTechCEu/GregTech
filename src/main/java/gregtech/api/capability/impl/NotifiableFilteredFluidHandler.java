package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import java.util.ArrayList;
import java.util.List;

public class NotifiableFilteredFluidHandler extends FilteredFluidHandler implements INotifiableHandler {

    List<MetaTileEntity> entitiesToNotify = new ArrayList<>();
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
    public List<MetaTileEntity> getNotifiableMetaTileEntities() {
        return this.entitiesToNotify;
    }
}
