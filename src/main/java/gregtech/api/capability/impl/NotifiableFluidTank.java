package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fluids.FluidTank;

import java.util.HashSet;

public class NotifiableFluidTank extends FluidTank implements INotifiableHandler {

    HashSet<MetaTileEntity> entityToSetDirty = new HashSet<>();
    private final boolean isExport;

    public NotifiableFluidTank(int capacity, MetaTileEntity entityToSetDirty, boolean isExport) {
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
    public HashSet<MetaTileEntity> getNotifiableMetaTileEntities(){
        return this.entityToSetDirty;
    }
}
