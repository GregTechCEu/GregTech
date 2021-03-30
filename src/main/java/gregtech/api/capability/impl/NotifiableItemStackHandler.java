package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;

public class NotifiableItemStackHandler extends ItemStackHandler implements IItemHandlerModifiable, INotifiableHandler {

    HashSet<MetaTileEntity> entityToSetDirty = new HashSet<>();
    private final boolean isExport;

    public NotifiableItemStackHandler(int slots, MetaTileEntity entityToSetDirty, boolean isExport) {
        super(slots);
        this.entityToSetDirty.add(entityToSetDirty);
        this.isExport = isExport;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        notifyMetaTileEntitiesOfChange(isExport);
    }

    @Override
    public HashSet<MetaTileEntity> getNotifiableMetaTileEntities() {
        return this.entityToSetDirty;
    }
}
