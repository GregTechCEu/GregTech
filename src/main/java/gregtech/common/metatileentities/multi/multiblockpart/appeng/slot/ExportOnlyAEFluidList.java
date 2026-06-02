package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import gregtech.api.metatileentity.MetaTileEntity;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import org.jetbrains.annotations.NotNull;

public class ExportOnlyAEFluidList implements IExportOnlyAEStackList<IAEFluidStack> {

    protected final int size;
    protected ExportOnlyAEFluidSlot[] inventory;

    public ExportOnlyAEFluidList(MetaTileEntity holder, int slots, MetaTileEntity entityToNotify) {
        this.size = slots;
        createInventory(holder, entityToNotify);
    }

    protected void createInventory(MetaTileEntity holder, MetaTileEntity entityToNotify) {
        this.inventory = new ExportOnlyAEFluidSlot[size];
        for (int i = 0; i < size; i++) {
            this.inventory[i] = new ExportOnlyAEFluidSlot(holder, entityToNotify);
        }
    }

    public @NotNull ExportOnlyAEFluidSlot @NotNull [] getInventory() {
        return inventory;
    }

    public void clearConfig() {
        for (var slot : inventory) {
            slot.setConfig(null);
            slot.setStock(null);
        }
    }

    public boolean hasStackInConfig(FluidStack stack, boolean checkExternal) {
        if (stack == null) return false;
        for (ExportOnlyAEFluidSlot slot : inventory) {
            IAEFluidStack config = slot.getConfig();
            if (config != null && config.getFluid().equals(stack.getFluid())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAutoPull() {
        return false;
    }

    public boolean isStocking() {
        return false;
    }

    public boolean ownsSlot(ExportOnlyAEFluidSlot testSlot) {
        for (var slot : inventory) {
            if (slot == testSlot) {
                return true;
            }
        }
        return false;
    }
}
