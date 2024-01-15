package gregtech.common.covers.filter;

import gregtech.api.gui.Widget;
import gregtech.api.mui.GTGuis;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.MouseData;
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

    private final SimpleFluidFilterReader filterReader;

    public SimpleFluidFilter(ItemStack stack) {
        this.filterReader = new SimpleFluidFilterReader(stack, MAX_FLUID_SLOTS);
    }

    @Override
    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
        this.markDirty();
    }

    @Override
    public void setMaxConfigurableFluidSize(int maxSize) {
        for (int i = 0; i < filterReader.getSlots(); i++) {
            filterReader.getFluidTank(i).setCapacity(maxSize);
        }
    }

    public boolean isBlacklist() {
        return this.filterReader.isBlacklistFilter();
    }

    public void setBlacklistFilter(boolean blacklist) {
        this.filterReader.setBlacklistFilter(blacklist);
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
        return GTGuis.createPanel(getContainerStack(), 100, 100);
    }

    @Override
    public @NotNull ParentWidget<?> createWidgets(GuiSyncManager syncManager) {
        FluidSlotSyncHandler[] syncHandlers = new FluidSlotSyncHandler[MAX_FLUID_SLOTS];
        for (int i = 0; i < syncHandlers.length; i++) {
            var tank = this.filterReader.getFluidTank(i);
            syncHandlers[i] = new FixedFluidSlotSH(tank).phantom(true);
            syncHandlers[i].setValue(tank.getFluid(), false, false);
        }

        return new Column().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new FluidSlot()
                                .syncHandler(syncHandlers[i]))
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
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return checkInputFluid(fluidStack);
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            widgetGroup.accept((new gregtech.api.gui.widgets.PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    filterReader.getFluidTank(i)::getFluid, filterReader.getFluidTank(i)::setFluid))
                            .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT).showTipSupplier(this::shouldShowTip));
        }
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
        return limit;
    }
    protected static class SimpleFluidFilterReader extends BaseFilterReader {

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

    public static class WritableFluidTank implements IFluidTank {

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

    public class FixedFluidSlotSH extends FluidSlotSyncHandler {
        @Nullable
        private FluidStack lastStoredPhantomFluid;

        public FixedFluidSlotSH(IFluidTank fluidTank) {
            super(fluidTank);
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
        public void tryClickPhantom(MouseData mouseData) {
            EntityPlayer player = getSyncManager().getPlayer();
            ItemStack currentStack = player.inventory.getItemStack();
            FluidStack currentFluid = this.getFluidTank().getFluid();
            IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

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
                        toFill.amount = this.controlsAmount() ? 1000 : 1;
                        this.getFluidTank().fill(toFill, true);
                    }
                }
            } else if (mouseData.mouseButton == 2 && currentFluid != null && this.canDrainSlot()) {
                this.getFluidTank().drain(mouseData.shift ? Integer.MAX_VALUE : 1000, true);
            }
        }

        @Override
        public void tryScrollPhantom(MouseData mouseData) {
            FluidStack currentFluid = this.getFluidTank().getFluid();
            int amount = mouseData.mouseButton;
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
                return;
            }
            if (amount > 0 && this.controlsAmount()) {
                FluidStack toFill = currentFluid.copy();
                toFill.amount = amount;
                this.getFluidTank().fill(toFill, true);
            } else if (amount < 0) {
                this.getFluidTank().drain(-amount, true);
            }
        }
    }
}
