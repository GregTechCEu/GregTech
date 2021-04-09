package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.LinkedList;

public class NotifiableItemStackHandler extends ItemStackHandler implements IItemHandlerModifiable, INotifiableHandler {

    LinkedList<MetaTileEntity> entitiesToNotify = new LinkedList<>();
    private final boolean isExport;

    public NotifiableItemStackHandler(int slots, MetaTileEntity entityToNotify, boolean isExport) {
        super(slots);
        this.entitiesToNotify.add(entityToNotify);
        this.isExport = isExport;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        notifyMetaTileEntitiesOfChange(isExport);
    }

    @Override
    public LinkedList<MetaTileEntity> getNotifiableMetaTileEntities() {
        return this.entitiesToNotify;
    }
}
