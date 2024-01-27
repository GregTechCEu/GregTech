package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SimpleFluidFilter extends FluidFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    private final SimpleFluidFilterReader filterReader;

    public SimpleFluidFilter(ItemStack stack) {
        this.filterReader = new SimpleFluidFilterReader(stack, MAX_FLUID_SLOTS);
        setFilterReader(this.filterReader);
    }

    @Override
    @Deprecated
    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
        this.markDirty();
    }

    @Override
    public ItemStack getContainerStack() {
        return this.filterReader.getContainer();
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_fluid_filter", 98, 81)
                .padding(4)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel(getContainerStack(), 176, 168);
    }

    @Override
    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        return new Row().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new FluidSlot()
                                .syncHandler(new FixedFluidSlotSH(filterReader.getFluidTank(i)).phantom(true)))
                        .build().marginRight(4))
                .child(super.createWidgets(syncManager));
    }

    @Override
    public MatchResult<FluidStack> match(FluidStack toMatch) {
        int index = -1;
        FluidStack returnable = null;
        for (int i = 0; i < filterReader.getSlots(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(toMatch)) {
                index = i;
                returnable = fluid.copy();
                break;
            }
        }
        return createResult(index != -1, returnable, index);
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        for (int i = 0; i < filterReader.getSlots(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            widgetGroup.accept((new gregtech.api.gui.widgets.PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    filterReader.getFluidTank(i)))
                            .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT));
        }
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter() && getMaxTransferSize() > 0;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList filterSlots = tagCompound.getTagList("FluidFilter", 10);
        for (int i = 0; i < this.filterReader.getSlots(); i++) {
            NBTTagCompound stackTag = filterSlots.getCompoundTagAt(i);
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
            if (fluidStack == null) continue;
            this.filterReader.getFluidTank(i).setFluid(fluidStack);
        }
    }

    @Override
    public int getTransferLimit(FluidStack fluidStack, int transferSize) {
        int limit = 0;

        for (int i = 0; i < this.filterReader.getSlots(); i++) {
            var fluid = this.filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                limit = fluid.amount;
            }
        }
        return isBlacklistFilter() ? transferSize : limit;
    }

    protected static class SimpleFluidFilterReader extends BaseFluidFilterReader {

        protected WritableFluidTank[] fluidTanks;
        protected static final String CAPACITY = "Capacity";

        public SimpleFluidFilterReader(ItemStack container, int slots) {
            super(container, slots);
            fluidTanks = new WritableFluidTank[slots];
            for (int i = 0; i < fluidTanks.length; i++) {
                fluidTanks[i] = new WritableFluidTank(this, getItemsNbt().getCompoundTagAt(i));
            }
            setCapacity(getStackTag().hasKey(CAPACITY) ? getCapacity() : 1000);
        }

        public void setCapacity(int capacity) {
            getStackTag().setInteger(CAPACITY, capacity);
        }

        public int getCapacity() {
            return getStackTag().getInteger(CAPACITY);
        }

        public WritableFluidTank getFluidTank(int i) {
            return fluidTanks[i];
        }

        public void setFluidAmounts(int amount) {
            for (int i = 0; i < getSlots(); i++) {
                getFluidTank(i).setFluidAmount(amount);
            }
        }

        @Override
        public void onTransferRateChange() {
            for (int i = 0; i < getSlots(); i++) {
                var stack = getFluidStack(i);
                if (stack == null) continue;
                getFluidTank(i).setFluidAmount(Math.min(stack.amount, getMaxTransferRate()));
            }
            setCapacity(getMaxTransferRate());
        }
    }

    public static class WritableFluidTank extends FluidTank {

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
        public FluidStack getFluid() {
            return FluidStack.loadFluidStackFromNBT(getFluidTag());
        }

        @Override
        public void setFluid(@Nullable FluidStack stack) {
            if (stack == null) {
                this.fluidTank.setTag(FLUID, new NBTTagCompound());
            } else {
                this.fluidTank.setTag(FLUID, stack.writeToNBT(getFluidTag()));
            }
        }

        protected boolean showAmount() {
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

        @SuppressWarnings("DataFlowIssue")
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (isEmpty()) return null;
            FluidStack fluid = getFluid();

            fluid.amount -= Math.min(fluid.amount, maxDrain);

            setFluidAmount(fluid.amount);

            return fluid;
        }
    }

    public static class FixedFluidSlotSH extends FluidSlotSyncHandler {

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
            if (getFluidTank() instanceof WritableFluidTank writableFluidTank) {
                return writableFluidTank.showAmount();
            }
            return super.controlsAmount();
        }
    }
}
