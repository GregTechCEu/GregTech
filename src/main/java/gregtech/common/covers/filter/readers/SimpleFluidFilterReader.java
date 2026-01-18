package gregtech.common.covers.filter.readers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.Nullable;

public class SimpleFluidFilterReader extends BaseFilterReader {

    protected WritableFluidTank[] fluidTanks;
    protected static final String CAPACITY = "Capacity";

    protected static final String LEGACY_FLUIDFILTER_KEY = "FluidFilter";

    public SimpleFluidFilterReader(ItemStack container, int slots) {
        super(container, slots);
        fluidTanks = new WritableFluidTank[slots];
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
        if (!getStackTag().hasKey(CAPACITY))
            getStackTag().setInteger(CAPACITY, 1000);
        return getStackTag().getInteger(CAPACITY);
    }

    public WritableFluidTank getFluidTank(int i) {
        if (fluidTanks[i] == null) {
            fluidTanks[i] = new WritableFluidTank(i);
        }
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
    public void handleLegacyNBT(NBTTagCompound tag) {
        super.handleLegacyNBT(tag);
        NBTTagCompound legacyFilter = tag.getCompoundTag(KEY_LEGACY_FILTER);

        NBTTagList filterSlots = legacyFilter.getTagList(LEGACY_FLUIDFILTER_KEY, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < filterSlots.tagCount(); i++) {
            NBTTagCompound stackTag = filterSlots.getCompoundTagAt(i);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
            if (fluidStack == null) continue;
            int slot = stackTag.getInteger("Slot");
            getFluidTank(slot).setFluid(fluidStack);
        }
    }

    public class WritableFluidTank implements IFluidTank {

        protected static final String FLUID_AMOUNT = "Amount";
        protected static final String FLUID = "Fluid";
        protected static final String EMPTY = "Empty";

        private final int index;

        public WritableFluidTank(int index) {
            this.index = index;
        }

        private NBTTagCompound getTank() {
            return getInventoryNbt().getCompoundTagAt(this.index);
        }

        public void setFluidAmount(int amount) {
            if (amount <= 0) {
                setFluid(null);
            } else if (this.getTank().hasKey(FLUID)) {
                this.getTank()
                        .getCompoundTag(FLUID)
                        .setInteger(FLUID_AMOUNT, amount);
                markDirty();
            }
        }

        public boolean isEmpty() {
            return !this.getTank().hasKey(FLUID);
        }

        protected @Nullable NBTTagCompound getFluidTag() {
            if (isEmpty()) {
                return null;
            }

            return this.getTank().getCompoundTag(FLUID);
        }

        @Override
        public @Nullable FluidStack getFluid() {
            return FluidStack.loadFluidStackFromNBT(getFluidTag());
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        // @Override
        public void setFluid(@Nullable FluidStack stack) {
            if (stack == null) {
                this.getTank().removeTag(FLUID);
            } else {
                this.getTank().setTag(FLUID, stack.writeToNBT(new NBTTagCompound()));
            }
            markDirty();
        }

        @Override
        public int getFluidAmount() {
            return this.getTank()
                    .getCompoundTag(FLUID)
                    .getInteger(FLUID_AMOUNT);
        }

        @Override
        public int getCapacity() {
            return SimpleFluidFilterReader.this.getCapacity();
        }

        // getFluid() is checked for nullability, suppress
        @SuppressWarnings("DataFlowIssue")
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            // todo this class and filter readers really should not be handling show amount
            // in a future pr
            if (isEmpty() || !getFluid().isFluidEqual(resource)) {
                setFluid(resource);
                if (!shouldShowAmount()) setFluidAmount(1);
                return resource.amount;
            } else if (shouldShowAmount()) {
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
