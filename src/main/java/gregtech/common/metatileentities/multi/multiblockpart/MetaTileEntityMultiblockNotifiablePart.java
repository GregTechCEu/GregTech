package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityMultiblockNotifiablePart extends MetaTileEntityMultiblockPart {

    protected final boolean isExportHatch;

    public MetaTileEntityMultiblockNotifiablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
    }

    private @NotNull List<INotifiableHandler> getItemHandlers() {
        List<INotifiableHandler> notifiables = new ArrayList<>();
        IItemHandlerModifiable mteHandler = isExportHatch ? getExportItems() : getImportItems();
        if (mteHandler instanceof INotifiableHandler notifiable) {
            notifiables.add(notifiable);
        } else if (mteHandler instanceof ItemHandlerList list) {
            for (IItemHandler handler : list.getBackingHandlers()) {
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiables.add(notifiable);
                }
            }
        }
        if (getItemInventory() instanceof INotifiableHandler notifiable) {
            notifiables.add(notifiable);
        }
        return notifiables;
    }

    private @Nullable FluidTankList getFluidHandlers() {
        if (isExportHatch) {
            FluidTankList exports = getExportFluids();
            return exports.getFluidTanks().isEmpty() ? null : exports;
        } else {
            FluidTankList imports = getImportFluids();
            return imports.getFluidTanks().isEmpty() ? null : imports;
        }
    }

    private List<INotifiableHandler> getPartHandlers() {
        List<INotifiableHandler> handlerList = new ArrayList<>();

        for (INotifiableHandler notif : getItemHandlers()) {
            if (notif.size() > 0) {
                handlerList.add(notif);
            }
        }

        FluidTankList fluidTankList = getFluidHandlers();
        if (fluidTankList != null) {
            for (IFluidTank fluidTank : fluidTankList) {
                if (fluidTank instanceof IMultipleTankHandler.ITankEntry entry) {
                    fluidTank = entry.getDelegate();
                }
                if (fluidTank instanceof INotifiableHandler iNotifiableHandler) {
                    handlerList.add(iNotifiableHandler);
                }
            }
        }

        return handlerList;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.addNotifiableMetaTileEntity(controllerBase);
            handler.addToNotifiedList(this, handler, isExportHatch);
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.removeNotifiableMetaTileEntity(controllerBase);
        }
    }
}
