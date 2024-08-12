package gregtech.api.util.virtualregistry.entries;

import gregtech.api.util.virtualregistry.EntryTypes;
import gregtech.api.util.virtualregistry.VirtualEntry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

public class VirtualTank extends VirtualEntry implements IFluidTank, IFluidHandler {

    protected static final String CAPACITY_KEY = "capacity";
    protected static final String FLUID_KEY = "fluid";
    private static final int DEFAULT_CAPACITY = 64000; // 64B

    @Nullable
    private FluidStack fluidStack = null;
    private int capacity;
    private final IFluidTankProperties[] props = new IFluidTankProperties[] {
            createProperty(this)
    };

    public VirtualTank(int capacity) {
        this.capacity = capacity;
    }

    public VirtualTank() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    public FluidStack getFluid() {
        return this.fluidStack;
    }

    public void setFluid(FluidStack fluid) {
        this.fluidStack = fluid;
    }

    @Override
    public int getFluidAmount() {
        return fluidStack == null ? 0 : fluidStack.amount;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.props;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualTank other)) return false;
        if (this.fluidStack == null && other.fluidStack == null)
            return super.equals(o);
        if (this.fluidStack == null || other.fluidStack == null)
            return false;
        if (this.fluidStack.isFluidStackIdentical(other.fluidStack))
            return super.equals(o);

        return false;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var tag = super.serializeNBT();
        tag.setInteger(CAPACITY_KEY, this.capacity);

        if (this.fluidStack != null)
            tag.setTag(FLUID_KEY, this.fluidStack.writeToNBT(new NBTTagCompound()));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.capacity = nbt.getInteger(CAPACITY_KEY);

        if (nbt.hasKey(FLUID_KEY))
            setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FLUID_KEY)));
    }

    @Override
    public int fill(FluidStack fluidStack, boolean doFill) {
        if (fluidStack == null || fluidStack.amount <= 0 ||
                (this.fluidStack != null && !fluidStack.isFluidEqual(this.fluidStack)))
            return 0;

        int fillAmt = Math.min(fluidStack.amount, getCapacity() - this.getFluidAmount());

        if (doFill) {
            if (this.fluidStack == null) {
                this.fluidStack = new FluidStack(fluidStack, fillAmt);
            } else {
                this.fluidStack.amount += fillAmt;
            }
        }
        return fillAmt;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return resource == null || !resource.isFluidEqual(this.fluidStack) ? null : drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int amount, boolean doDrain) {
        if (this.fluidStack == null || amount <= 0)
            return null;

        int drainAmt = Math.min(this.getFluidAmount(), amount);
        FluidStack drainedFluid = new FluidStack(this.fluidStack, drainAmt);
        if (doDrain) {
            this.fluidStack.amount -= drainAmt;
            if (this.fluidStack.amount <= 0) {
                this.fluidStack = null;
            }
        }
        return drainedFluid;
    }

    private static IFluidTankProperties createProperty(VirtualTank tank) {
        return new IFluidTankProperties() {

            @Nullable
            @Override
            public FluidStack getContents() {
                FluidStack contents = tank.getFluid();
                return contents == null ? null : contents.copy();
            }

            @Override
            public int getCapacity() {
                return tank.getCapacity();
            }

            @Override
            public boolean canFill() {
                return true;
            }

            @Override
            public boolean canDrain() {
                return true;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                return true;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return true;
            }
        };
    }
}
