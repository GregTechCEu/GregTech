package gregtech.common.metatileentities.multi.multiblockpart.appeng.slot;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import appeng.api.storage.data.IAEFluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExportOnlyAEFluidSlot extends ExportOnlyAESlot<IAEFluidStack>
                                   implements IFluidTank, INotifiableHandler, IFluidHandler {

    private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private MetaTileEntity holder;

    public ExportOnlyAEFluidSlot(MetaTileEntity holder, IAEFluidStack config, IAEFluidStack stock, MetaTileEntity mte) {
        super(config, stock);
        this.holder = holder;
        this.notifiableEntities.add(mte);
    }

    public ExportOnlyAEFluidSlot(MetaTileEntity holder, MetaTileEntity entityToNotify) {
        this(holder, null, null, entityToNotify);
    }

    public ExportOnlyAEFluidSlot() {
        super();
    }

    @Override
    public IAEFluidStack requestStack() {
        IAEFluidStack result = super.requestStack();
        if (result instanceof WrappedFluidStack) {
            return ((WrappedFluidStack) result).getAEStack();
        } else {
            return result;
        }
    }

    @Override
    public IAEFluidStack exceedStack() {
        IAEFluidStack result = super.exceedStack();
        if (result instanceof WrappedFluidStack) {
            return ((WrappedFluidStack) result).getAEStack();
        } else {
            return result;
        }
    }

    @Override
    public void addStack(IAEFluidStack stack) {
        if (this.stock == null) {
            this.stock = WrappedFluidStack.fromFluidStack(stack.getFluidStack());
        } else {
            this.stock.add(stack);
        }
        trigger();
    }

    @Override
    public void setStack(IAEFluidStack stack) {
        if (this.stock == null && stack == null) {
            return;
        } else if (stack == null) {
            this.stock = null;
        } else if (this.stock == null || this.stock.getFluid() != stack.getFluid()) {
            this.stock = WrappedFluidStack.fromFluidStack(stack.getFluidStack());
        } else if (this.stock.getStackSize() != stack.getStackSize()) {
            this.stock.setStackSize(stack.getStackSize());
        } else return;
        trigger();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(CONFIG_TAG)) {
            this.config = WrappedFluidStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
        }
        if (nbt.hasKey(STOCK_TAG)) {
            this.stock = WrappedFluidStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
        }
    }

    @Nullable
    @Override
    public FluidStack getFluid() {
        if (this.stock instanceof WrappedFluidStack wrappedFluidStack) {
            return wrappedFluidStack.getDefinition();
        }
        return null;
    }

    @Override
    public int getFluidAmount() {
        return this.stock != null ? (int) this.stock.getStackSize() : 0;
    }

    @Override
    public int getCapacity() {
        // Its capacity is always 0.
        return 0;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] {
                new FluidTankProperties(this.getFluid(), 0)
        };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (this.getFluid() != null && this.getFluid().isFluidEqual(resource)) {
            return this.drain(resource.amount, doDrain);
        }
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (this.stock == null) {
            return null;
        }
        int drained = (int) Math.min(this.stock.getStackSize(), maxDrain);
        FluidStack result = new FluidStack(this.stock.getFluid(), drained);
        if (doDrain) {
            this.stock.decStackSize(drained);
            if (this.stock.getStackSize() == 0) {
                this.stock = null;
            }
            trigger();
        }
        return result;
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }

    protected void trigger() {
        for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                this.addToNotifiedList(metaTileEntity, this, false);
            }
        }
        if (holder != null) {
            holder.markDirty();
        }
    }

    @Override
    public @NotNull IConfigurableSlot<IAEFluidStack> copy() {
        return new ExportOnlyAEFluidSlot(
                this.holder,
                this.config == null ? null : this.config.copy(),
                this.stock == null ? null : this.stock.copy(),
                null);
    }

    protected MetaTileEntity getHolder() {
        return holder;
    }
}
