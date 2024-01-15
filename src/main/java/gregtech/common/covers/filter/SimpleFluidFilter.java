package gregtech.common.covers.filter;

import gregtech.api.gui.Widget;
import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SimpleFluidFilter extends FluidFilter {

    private static final int MAX_FLUID_SLOTS = 9;

//    protected final FluidTank[] fluidFilterTanks = new FluidTank[MAX_FLUID_SLOTS];

    private final SimpleFluidFilterReader filterReader;

    public SimpleFluidFilter(ItemStack stack) {
        this.filterReader = new SimpleFluidFilterReader(stack, MAX_FLUID_SLOTS);
//        for (int i = 0; i < MAX_FLUID_SLOTS; ++i) {
//            fluidFilterTanks[i] = new FluidTank(1000) {
//
//                @Override
//                public void setFluid(@Nullable FluidStack fluid) {
//                    super.setFluid(fluid);
//                    SimpleFluidFilter.this.markDirty();
//                }
//            };
//        }
    }

    @Override
    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
//        for (FluidTank fluidTank : fluidFilterTanks) {
//            if (fluidTank.getFluid() != null)
//                fluidTank.getFluid().amount = amount;
//        }
        this.markDirty();
    }

    @Override
    public void setMaxConfigurableFluidSize(int maxSize) {
        for (int i = 0; i < filterReader.getSlots(); i++) {
            filterReader.getFluidTank(i).setCapacity(maxSize);
        }
//        for (FluidTank fluidTank : fluidFilterTanks) {
//            fluidTank.setCapacity(maxSize);
//        }
    }

    @Override
    public ItemStack getContainerStack() {
        return this.filterReader.getContainer();
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_fluid_filter", 100, 100)
                .padding(4)
                .child(createWidgets(syncManager));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel(this.filterReader.getContainer(), 100, 100);
    }

    @Override
    public @NotNull ParentWidget<?> createWidgets(GuiSyncManager syncManager) {
        return new Column().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new FluidSlot()
                                .syncHandler(new FluidSlotSyncHandler(this.filterReader.getFluidTank(i))
                                        .phantom(true)))
                        .build());
    }

    @Override
    public void match(FluidStack toMatch) {
        boolean matched = false;
        int index = -1;
        for (int i = 0; i < filterReader.getSlots(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(toMatch)) {
                matched = true;
                index = i;
                break;
            }
        }
        this.onMatch(matched, toMatch.copy(), index);
//        for (int i = 0; i < fluidFilterTanks.length; i++) {
//            FluidTank fluidTank = fluidFilterTanks[i];
//            if (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(toMatch)) {
//                matched = true;
//                index = i;
//                break;
//            }
//        }
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return checkInputFluid(fluidStack);
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
//        for (int i = 0; i < 9; ++i) {
//            widgetGroup.accept((new PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
//                    this.fluidFilterTanks[i]))
//                            .setBackgroundTexture(GuiTextures.SLOT).showTipSupplier(this::shouldShowTip));
//        }
    }

    private boolean shouldShowTip() {
        return showTip;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
//        NBTTagList filterSlots = tagCompound.getTagList("FluidFilter", 10);
//        for (NBTBase nbtBase : filterSlots) {
//            NBTTagCompound stackTag = (NBTTagCompound) nbtBase;
//            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
//            this.fluidFilterTanks[stackTag.getInteger("Slot")].setFluid(fluidStack);
//        }
    }

    public boolean checkInputFluid(FluidStack fluidStack) {
        for (int i = 0; i < filterReader.getSlots(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getFluidTransferLimit(FluidStack fluidStack) {
        int limit = 0;

        for (int i = 0; i < this.filterReader.getSlots(); i++) {
            var fluid = this.filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                limit = fluid.amount;
            }
        }
//        for (FluidTank fluidTank : fluidFilterTanks) {
//            if (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(fluidStack)) {
//                limit = fluidTank.getFluid().amount;
//                break;
//            }
//        }
        return limit;
    }
    protected class SimpleFluidFilterReader extends BaseFilterReader {

        protected static final String KEY_FLUIDS = "FluidTank";
        public SimpleFluidFilterReader(ItemStack container, int slots) {
            super(container, slots);
        }

        @Override
        public void onMaxStackSizeChange() {

        }

        @Override
        public NBTTagList getItemsNbt() {
            NBTTagCompound nbt = getStackTag();
            if (!nbt.hasKey(KEY_FLUIDS)) {
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < getSlots(); i++) {
                    list.appendTag(new NBTTagCompound());
                }
                nbt.setTag(KEY_FLUIDS, list);
            }
            return nbt.getTagList(KEY_FLUIDS, Constants.NBT.TAG_COMPOUND);
        }

        public FluidStack getFluidStack(int i) {
            return getFluidTank(i).getFluid();
        }

        public WritableFluidTank getFluidTank(int i) {
            return new WritableFluidTank(getItemsNbt().getCompoundTagAt(i), 1000);
        }

        public void setFluidAmounts(int amount) {
            for (int i = 0; i < getSlots(); i++) {
                getFluidTank(i).setFluidAmount(amount);
            }
        }
    }

    protected class WritableFluidTank implements IFluidTank {

        private final NBTTagCompound fluidTank;
        protected static final String FLUID_AMOUNT = "Amount";
        protected static final String CAPACITY = "Capacity";
        protected static final String FLUID = "Fluid";
        protected static final String EMPTY = "Empty";
        public WritableFluidTank(NBTTagCompound fluidTank, int initialCapacity) {
            this.fluidTank = fluidTank;
            setCapacity(initialCapacity);
        }

        public void setCapacity(int capacity) {
            this.fluidTank.setInteger(CAPACITY, capacity);
        }

        public void setFluidAmount(int amount) {
            getFluidTag().setInteger(FLUID_AMOUNT, amount);
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
        public FluidStack getFluid() {
            return FluidStack.loadFluidStackFromNBT(getFluidTag());
        }

        public void setFluid(@Nullable FluidStack stack) {
            if (stack == null) {
                this.fluidTank.setTag(FLUID, new NBTTagCompound());
            } else {
                this.fluidTank.setTag(FLUID, stack.writeToNBT(getFluidTag()));
            }
        }

        @Override
        public int getFluidAmount() {
            return isEmpty() ? 0 : getFluidTag().getInteger(FLUID_AMOUNT);
        }

        @Override
        public int getCapacity() {
            return this.fluidTank.getInteger(CAPACITY);
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(getFluid(), getCapacity());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (isEmpty() || !getFluid().isFluidEqual(resource)) {
                setFluid(resource);
                return resource.amount;
            } else {
                var fluid = getFluid();
                int accepted = Math.min(resource.amount, getCapacity() - fluid.amount);
                fluid.amount += accepted;
                setFluid(fluid);
                return accepted;
            }
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (isEmpty()) return null;
            var fluid = getFluid();
            fluid.amount = Math.min(fluid.amount, maxDrain);

            var copy = getFluid();
            copy.amount -= fluid.amount;

            if (copy.amount == 0)
                setFluid(null);
            else
                setFluid(copy);

            return fluid;
        }
    }
}
