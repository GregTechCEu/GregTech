package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import java.util.HashSet;

public class NotifiableFilteredFluidHandler extends FilteredFluidHandler implements INotifiableHandler {

    HashSet<MetaTileEntity> entityToSetDirty = new HashSet<>();
    private final boolean isExport;

    public NotifiableFilteredFluidHandler(int capacity, MetaTileEntity entityToSetDirty, boolean isExport) {
        super(capacity);
        this.entityToSetDirty.add(entityToSetDirty);
        this.isExport = isExport;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        notifyMetaTileEntitiesOfChange(isExport);
    }

    @Override
    public HashSet<MetaTileEntity> getNotifiableMetaTileEntities() {
        return this.entityToSetDirty;
    }
}
