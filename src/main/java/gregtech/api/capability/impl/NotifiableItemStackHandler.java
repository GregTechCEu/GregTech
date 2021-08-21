package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class NotifiableItemStackHandler extends ItemStackHandler implements IItemHandlerModifiable, INotifiableHandler {

    MetaTileEntity notifiableEntity;
    private final boolean isExport;

    public NotifiableItemStackHandler(int slots, MetaTileEntity entityToNotify, boolean isExport) {
        super(slots);
        this.notifiableEntity = entityToNotify;
        this.isExport = isExport;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        addToNotifiedList(notifiableEntity, this, isExport);
    }

    @Override
    public void setNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntity = metaTileEntity;
    }
}
