package gregtech.api.capability.impl;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NotifiableItemStackHandler extends GTItemStackHandler
                                        implements IItemHandlerModifiable, INotifiableHandler {

    private final @NotNull List<@NotNull MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    /**
     * @param metaTileEntity MetaTileEntity to mark as dirty during {@link #onContentsChanged(int)}
     * @param slots          size of this handler
     * @param isExport       if this handler is considered an export for notified MTEs
     */
    public NotifiableItemStackHandler(MetaTileEntity metaTileEntity, int slots,
                                      boolean isExport) {
        this(metaTileEntity, slots, null, isExport);
    }

    /**
     * @param metaTileEntity MetaTileEntity to mark as dirty during {@link #onContentsChanged(int)}
     * @param slots          size of this handler
     * @param entityToNotify a MetaTileEntity to add to the list to notify after {@link #onContentsChanged(int)}
     * @param isExport       if this handler is considered an export for notified MTEs
     */
    public NotifiableItemStackHandler(MetaTileEntity metaTileEntity, int slots, MetaTileEntity entityToNotify,
                                      boolean isExport) {
        super(metaTileEntity, slots);
        addNotifiableMetaTileEntity(entityToNotify);
        this.isExport = isExport;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            if (metaTileEntity.isValid()) {
                addToNotifiedList(metaTileEntity, getHandler(slot), isExport);
            }
        }
    }

    protected Object getHandler(int slot) {
        return this;
    }

    @Override
    public void addNotifiableMetaTileEntity(@Nullable MetaTileEntity metaTileEntity) {
        if (metaTileEntity != null) {
            this.notifiableEntities.add(metaTileEntity);
        }
    }

    @Override
    public void removeNotifiableMetaTileEntity(@Nullable MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }
}
