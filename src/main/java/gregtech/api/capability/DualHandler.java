package gregtech.api.capability;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.ItemStackHashStrategy;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DualHandler extends MultipleTankHandler implements IItemHandlerModifiable, INotifiableHandler {

    @NotNull
    private static final ItemStackHashStrategy strategy = ItemStackHashStrategy.comparingAll();

    @NotNull
    protected IItemHandlerModifiable itemDelegate;
    @NotNull
    protected MultipleTankHandler fluidDelegate;

    private final List<Entry> unwrapped;

    List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final boolean isExport;

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull MultipleTankHandler fluidDelegate,
                       boolean isExport) {
        this.itemDelegate = itemDelegate;
        this.fluidDelegate = fluidDelegate;
        this.isExport = isExport;

        List<Entry> list = new ArrayList<>();
        for (Entry tank : this.fluidDelegate) {
            list.add(wrap(tank));
        }
        this.unwrapped = list;
    }

    public DualHandler(@NotNull IItemHandlerModifiable itemDelegate,
                       @NotNull IFluidTank fluidTank,
                       boolean isExport) {
        this(itemDelegate, new FluidTankList(false, fluidTank), isExport);
    }

    @Override
    protected Entry wrap(IFluidTank tank) {
        return tank instanceof DualEntry ? (DualEntry) tank : new DualEntry(this, super.wrap(tank));
    }

    public boolean isExport() {
        return this.isExport;
    }

    @Override
    public int getSlots() {
        return itemDelegate.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return itemDelegate.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        var remainder = itemDelegate.insertItem(slot, stack, simulate);
        if (!simulate && !strategy.equals(remainder, stack))
            onContentsChanged();
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = itemDelegate.extractItem(slot, amount, simulate);
        if (!simulate && !extracted.isEmpty())
            onContentsChanged();
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemDelegate.getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        var oldStack = itemDelegate.getStackInSlot(slot);
        itemDelegate.setStackInSlot(slot, stack);
        if (!strategy.equals(oldStack, stack))
            onContentsChanged();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.fluidDelegate.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = fluidDelegate.fill(resource, doFill);
        if (doFill && filled > 0) onContentsChanged();
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        var drained = fluidDelegate.drain(resource, doDrain);
        if (doDrain && drained != null) onContentsChanged();
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        var drained = fluidDelegate.drain(maxDrain, doDrain);
        if (doDrain && drained != null) onContentsChanged();
        return drained;
    }

    @Override
    public @NotNull List<Entry> getFluidTanks() {
        return this.unwrapped;
    }

    @Override
    public int size() {
        return this.fluidDelegate.size();
    }

    @Override
    public @NotNull Entry getTankAt(int index) {
        return this.unwrapped.get(index);
    }

    @Override
    public boolean allowSameFluidFill() {
        return this.fluidDelegate.allowSameFluidFill();
    }

    public void onContentsChanged(Object handler) {
        for (MetaTileEntity metaTileEntity : notifiableEntities) {
            addToNotifiedList(metaTileEntity, handler, isExport);
        }
    }

    public void onContentsChanged() {
        onContentsChanged(this);
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null || this.notifiableEntities.contains(metaTileEntity))
            return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    public @NotNull IItemHandlerModifiable getItemDelegate() {
        return itemDelegate;
    }

    public @NotNull MultipleTankHandler getFluidDelegate() {
        return fluidDelegate;
    }

    public static class DualEntry extends Entry implements INotifiableHandler {

        @NotNull
        private final DualHandler parent;

        public DualEntry(@NotNull DualHandler parent, @NotNull Entry tank) {
            super(tank, tank.getParentHandler());
            this.parent = parent;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = getDelegate().fill(resource, doFill);
            if (doFill && filled > 0)
                this.parent.onContentsChanged(this);
            return filled;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            var drained = getDelegate().drain(maxDrain, doDrain);
            if (doDrain && drained != null)
                this.parent.onContentsChanged(this);
            return drained;
        }

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.parent.addNotifiableMetaTileEntity(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.parent.removeNotifiableMetaTileEntity(metaTileEntity);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound root = new NBTTagCompound();
        if (this.itemDelegate instanceof ItemStackHandler handler) {
            root.setTag("item", handler.serializeNBT());
        }
        root.setTag("fluid", getFluidDelegate().serializeNBT());
        return root;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (this.itemDelegate instanceof ItemStackHandler handler) {
            handler.deserializeNBT(nbt.getCompoundTag("item"));
        }
        getFluidDelegate().deserializeNBT(nbt.getCompoundTag("fluid"));
    }
}
