package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidFilterContainer implements INBTSerializable<NBTTagCompound> {

    private final ItemStackHandler filterInventory;
    private final FluidFilterWrapper filterWrapper;

    private final IDirtyNotifiable dirtyNotifiable;
    private FluidFilter currentFluidFilter;
    private Supplier<Boolean> showTipSupplier;
    private int maxSize;

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable, int capacity) {
        this.filterWrapper = new FluidFilterWrapper(this); // for compat
        this.filterInventory = new ItemStackHandler(1) {

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return FilterTypeRegistry.isFluidFilter(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onLoad() {
                onFilterSlotChange(false);
            }

            @Override
            protected void onContentsChanged(int slot) {
                onFilterSlotChange(true);
            }
        };
        this.maxSize = capacity;
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable, Supplier<Boolean> showTip, int maxSize) {
        this(dirtyNotifiable, maxSize);
        setTipSupplier(showTip);
    }

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable, Supplier<Boolean> showTip) {
        this(dirtyNotifiable, 1000);
        setTipSupplier(showTip);
    }

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this(dirtyNotifiable, 1000);
        setTipSupplier(() -> false);
    }

    public void setFluidFilter(FluidFilter fluidFilter) {
        this.currentFluidFilter = fluidFilter;
        if (hasFluidFilter()) {
            currentFluidFilter.setDirtyNotifiable(dirtyNotifiable);
            currentFluidFilter.setMaxConfigurableFluidSize(maxSize);
        }
    }

    private Supplier<Boolean> shouldShowTip() {
        return showTipSupplier;
    }

    protected void setTipSupplier(Supplier<Boolean> supplier) {
        this.showTipSupplier = supplier;
    }

    public FluidFilter getFluidFilter() {
        return currentFluidFilter;
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    public FluidFilterWrapper getFilterWrapper() {
        return filterWrapper;
    }

    public boolean testFluidStack(FluidStack fluidStack) {
        return testFluidStack(fluidStack, !isBlacklistFilter());
    }

    public boolean testFluidStack(FluidStack fluidStack, boolean whitelist) {
        boolean result = true;
        if (hasFluidFilter()) {
            result = currentFluidFilter.test(fluidStack);
            if (!whitelist) {
                result = !result;
            }
        }
        return result;
    }

    public void initUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.pump.fluid_filter.title"));
        widgetGroup.accept(new SlotWidget(filterInventory, 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 15, widgetGroup);
        this.blacklistUI(y + 15, widgetGroup, () -> true);
    }

    public void initFilterUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupFluidFilter(y, this::getFluidFilter, shouldShowTip()));
    }

    public void blacklistUI(int y, Consumer<Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(this::hasFluidFilter);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 18, 18, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    public boolean hasFluidFilter() {
        return currentFluidFilter != null;
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasFluidFilter()) getFluidFilter().setBlacklistFilter(blacklistFilter);
    }

    public boolean isBlacklistFilter() {
        return hasFluidFilter() && getFluidFilter().isBlacklist();
    }

    protected void onFilterSlotChange(boolean notify) {
        ItemStack filterStack = filterInventory.getStackInSlot(0);
        int newId = FilterTypeRegistry.getFilterIdForStack(filterStack);
        int currentId = FilterTypeRegistry.getIdForFilter(getFluidFilter());

        if (!FilterTypeRegistry.isFluidFilter(filterStack)) {
            if (hasFluidFilter()) {
                setFluidFilter(null);
                setBlacklistFilter(false);
                if (notify)
                    onFilterInstanceChange();
            }
        } else if (currentId == -1 || newId != currentId) {
            setFluidFilter(FilterTypeRegistry.getFluidFilterForStack(filterStack));
            if (notify)
                onFilterInstanceChange();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("FilterInventory", filterInventory.serializeNBT());
//        tagCompound.setBoolean("IsBlacklist", getFluidFilter().isBlacklistFilter());
//        if (getFluidFilter() != null) {
//            NBTTagCompound filterInventory = new NBTTagCompound();
//            getFluidFilter().writeToNBT(filterInventory);
//            tagCompound.setTag("Filter", filterInventory);
//        }
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.filterInventory.deserializeNBT(tagCompound.getCompoundTag("FilterInventory"));
        this.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        if (getFluidFilter() != null) {
            this.getFluidFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
        }
    }
}
