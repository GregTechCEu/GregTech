package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.Nullable;

public class SimpleFluidFilterReader extends BaseFilterReader {

    protected WritableFluidTank[] fluidTanks;
    protected static final String CAPACITY = "Capacity";

    protected static final String KEY_FLUIDS = "FluidFilter";
    protected static final String BUCKET_ONLY = "BucketOnly";

    public SimpleFluidFilterReader(ItemStack container, int slots) {
        super(container, slots);
        fluidTanks = new WritableFluidTank[slots];
        for (int i = 0; i < fluidTanks.length; i++) {
            fluidTanks[i] = new WritableFluidTank(this, getInventoryNbt().getCompoundTagAt(i));
        }
        setCapacity(getStackTag().hasKey(CAPACITY) ? getCapacity() : 1000);
    }

    @Override
    public String getKey() {
        return KEY_FLUIDS;
    }

    public final void setBucketOnly(boolean bucketOnly) {
        getStackTag().setBoolean(BUCKET_ONLY, bucketOnly);
    }

    public final boolean shouldShowAmount() {
        return getMaxTransferRate() > 1;
    }

    @Nullable
    public FluidStack getFluidStack(int i) {
        return getFluidTank(i).getFluid();
    }

    public void setCapacity(int capacity) {
        getStackTag().setInteger(CAPACITY, capacity);
        markDirty();
    }

    public int getCapacity() {
        return getStackTag().getInteger(CAPACITY);
    }

    public WritableFluidTank getFluidTank(int i) {
        return fluidTanks[i];
    }

    public void setFluidAmounts(int amount) {
        for (int i = 0; i < getSize(); i++) {
            getFluidTank(i).setFluidAmount(amount);
        }
    }

    @Override
    public void onTransferRateChange() {
        for (int i = 0; i < getSize(); i++) {
            var stack = getFluidStack(i);
            if (stack == null) continue;
            getFluidTank(i).setFluidAmount(Math.min(stack.amount, getMaxTransferRate()));
        }
        setCapacity(getMaxTransferRate());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        NBTTagList filterSlots = nbt.getTagList(KEY_FLUIDS, 10);
        for (int i = 0; i < filterSlots.tagCount(); i++) {
            NBTTagCompound stackTag = filterSlots.getCompoundTagAt(i);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
            if (fluidStack == null) continue;
            getFluidTank(i).setFluid(fluidStack);
        }
    }

    public class WritableFluidTank extends FluidTank {

        private final NBTTagCompound fluidTank;
        private final SimpleFluidFilterReader filterReader;
        protected static final String FLUID_AMOUNT = "Amount";
        protected static final String FLUID = "Fluid";
        protected static final String EMPTY = "Empty";

        protected WritableFluidTank(SimpleFluidFilterReader filterReader, NBTTagCompound fluidTank) {
            super(0);
            this.filterReader = filterReader;
            this.fluidTank = fluidTank;
        }

        public void setFluidAmount(int amount) {
            if (amount <= 0) {
                setFluid(null);
            } else {
                getFluidTag().setInteger(FLUID_AMOUNT, amount);
                markDirty();
            }
        }

        public boolean isEmpty() {
            return getFluidTag().isEmpty();
        }

        protected NBTTagCompound getFluidTag() {
            if (!this.fluidTank.hasKey(FLUID)) {
                this.fluidTank.setTag(FLUID, new NBTTagCompound());
            }

            return this.fluidTank.getCompoundTag(FLUID);
        }

        @Override
        public @Nullable FluidStack getFluid() {
            return FluidStack.loadFluidStackFromNBT(getFluidTag());
        }

        @Override
        public void setFluid(@Nullable FluidStack stack) {
            if (stack == null) {
                this.fluidTank.setTag(FLUID, new NBTTagCompound());
            } else {
                this.fluidTank.setTag(FLUID, stack.writeToNBT(getFluidTag()));
            }
            markDirty();
        }

        public boolean showAmount() {
            return filterReader.shouldShowAmount();
        }

        @Override
        public int getFluidAmount() {
            return getFluidTag().getInteger(FLUID_AMOUNT);
        }

        @Override
        public int getCapacity() {
            return this.filterReader.getCapacity();
        }

        // getFluid() is checked for nullability, suppress
        @SuppressWarnings("DataFlowIssue")
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (isEmpty() || !getFluid().isFluidEqual(resource)) {
                setFluid(resource);
                if (!showAmount()) setFluidAmount(1);
                return resource.amount;
            } else if (showAmount()) {
                var fluid = getFluid();
                int accepted = Math.min(resource.amount, getCapacity() - fluid.amount);
                fluid.amount += accepted;
                setFluid(fluid);
                return accepted;
            }
            return 0;
        }

        // getFluid() is checked for nullability, suppress
        @SuppressWarnings("DataFlowIssue")
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (isEmpty()) return null;
            FluidStack fluid = getFluid();

            fluid.amount -= Math.min(fluid.amount, maxDrain);

            setFluidAmount(fluid.amount);
            markDirty();
            return fluid;
        }
    }
}
