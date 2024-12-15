package gregtech.api.mui.sync;

import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import org.jetbrains.annotations.Nullable;

public class FixedFluidSlotSH extends FluidSlotSyncHandler {

    @Nullable
    private FluidStack lastStoredPhantomFluid;

    public FixedFluidSlotSH(IFluidTank fluidTank) {
        super(fluidTank);
        if (this.updateCacheFromSource(true) && fluidTank.getFluid() != null) {
            this.lastStoredPhantomFluid = fluidTank.getFluid().copy();
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        super.readOnServer(id, buf);
        if (id == 0) {
            var fluid = getFluidTank().getFluid();
            if (this.lastStoredPhantomFluid == null && fluid != null ||
                    (this.lastStoredPhantomFluid != null && !this.lastStoredPhantomFluid.isFluidEqual(fluid))) {
                this.lastStoredPhantomFluid = fluid;
            }
        }
    }

    @Override
    public void setValue(@Nullable FluidStack value, boolean setSource, boolean sync) {
        super.setValue(value, setSource, sync);
        if (setSource) {
            this.getFluidTank().drain(Integer.MAX_VALUE, true);
            if (!isFluidEmpty(value)) {
                this.getFluidTank().fill(value.copy(), true);
            }
        }
    }

    @Override
    public void tryClickPhantom(MouseData mouseData) {
        EntityPlayer player = getSyncManager().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        FluidStack currentFluid = this.getFluidTank().getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

        if (mouseData.mouseButton == 0) {
            if (currentStack.isEmpty() || fluidHandlerItem == null) {
                if (this.canDrainSlot()) {
                    this.getFluidTank().drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                }
            } else {
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                if ((this.controlsAmount() || currentFluid == null) && cellFluid != null) {
                    if (this.canFillSlot()) {
                        if (!this.controlsAmount()) {
                            cellFluid.amount = 1;
                        }
                        if (this.getFluidTank().fill(cellFluid, true) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                    }
                } else {
                    if (this.canDrainSlot()) {
                        this.getFluidTank().drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
                    }
                }
            }
        } else if (mouseData.mouseButton == 1) {
            if (this.canFillSlot()) {
                if (currentFluid != null) {
                    if (this.controlsAmount()) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.amount = 1000;
                        this.getFluidTank().fill(toFill, true);
                    }
                } else if (this.lastStoredPhantomFluid != null) {
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.amount = this.controlsAmount() ? 1 : toFill.amount;
                    this.getFluidTank().fill(toFill, true);
                }
            }
        } else if (mouseData.mouseButton == 2 && currentFluid != null && this.canDrainSlot()) {
            this.getFluidTank().drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
        }
        this.setValue(this.getFluidTank().getFluid(), false, true);
    }

    @Override
    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.getFluidTank().getFluid();
        int amount = mouseData.mouseButton;
        if (!this.controlsAmount()) {
            var fluid = getFluidTank().getFluid();
            int newAmt = amount == 1 ? 1 : 0;
            if (fluid != null && fluid.amount != newAmt) {
                fluid.amount = newAmt;
                setValue(fluid, true, true);
                return;
            }
        }
        if (mouseData.shift) {
            amount *= 10;
        }
        if (mouseData.ctrl) {
            amount *= 100;
        }
        if (mouseData.alt) {
            amount *= 1000;
        }
        if (currentFluid == null) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.amount = this.controlsAmount() ? amount : 1;
                this.getFluidTank().fill(toFill, true);
            }
            this.setValue(this.getFluidTank().getFluid(), false, true);
            return;
        }
        if (amount > 0) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = amount;
            this.getFluidTank().fill(toFill, true);
        } else if (amount < 0) {
            this.getFluidTank().drain(-amount, true);
        }
        this.setValue(this.getFluidTank().getFluid(), false, true);
    }

    @Override
    public boolean controlsAmount() {
        if (getFluidTank() instanceof SimpleFluidFilterReader.WritableFluidTank writableFluidTank) {
            return writableFluidTank.showAmount();
        }
        return super.controlsAmount();
    }
}
