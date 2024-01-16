package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidFilterContainer implements INBTSerializable<NBTTagCompound> {

    private final ItemStackHandler filterInventory;
    private final FluidFilterWrapper filterWrapper;

    private final IDirtyNotifiable dirtyNotifiable;
    private FluidFilter currentFluidFilter;
    private Supplier<Boolean> showTipSupplier;
    private Supplier<Integer> maxFluidSizer = () -> this.maxSize;
    private int maxSize;

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable, int capacity) {
        this.filterWrapper = new FluidFilterWrapper(this); // for compat
        this.maxSize = capacity;
        this.dirtyNotifiable = dirtyNotifiable;
        this.filterInventory = new ItemStackHandler(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onLoad() {
                onFilterSlotChange(false);
            }
        };
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
            currentFluidFilter.setMaxStackSizer(maxFluidSizer);
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

    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new gregtech.api.gui.widgets.LabelWidget(10, y, "cover.pump.fluid_filter.title"));
        widgetGroup.accept(new gregtech.api.gui.widgets.SlotWidget(filterInventory, 0, 10, y + 15)
                .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT, gregtech.api.gui.GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 15, widgetGroup);
        this.blacklistUI(y + 15, widgetGroup, () -> true);
    }

    public void initFilterUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupFluidFilter(y, this::getFluidFilter, shouldShowTip()));
    }

    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        gregtech.api.gui.widgets.ServerWidgetGroup blacklistButton = new gregtech.api.gui.widgets.ServerWidgetGroup(this::hasFluidFilter);
        blacklistButton.addWidget(new gregtech.api.gui.widgets.ToggleButtonWidget(144, y, 18, 18, gregtech.api.gui.GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    /** Uses Cleanroom MUI*/
    public IWidget initUI(ModularPanel main, GuiSyncManager manager) {
        var panel = new PanelSyncHandler(main) {
            @Override
            public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
//                getFluidFilter().setMaxStackSizer(stackSizer);
                return getFluidFilter().createPopupPanel(syncManager);
            }
        };
        manager.syncValue("filter_panel", panel);

        return new Row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(filterInventory, 0)
                                .filter(FilterTypeRegistry::isFluidFilter)
                                .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                    if (newItem.isEmpty() || FilterTypeRegistry.isFluidFilter(newItem)) {
                                        onFilterSlotChange(true);
                                    }
                                })
                                .singletonSlotGroup(101))
                        .onUpdateListener(w -> {
                            if (!hasFluidFilter() && panel.isPanelOpen()) {
                                panel.closePanel();
                            }
                        }, true)
                        .size(18).marginRight(4)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY))
                .child(new ButtonWidget<>()
                        .setEnabledIf(w -> hasFluidFilter())
                        .onMousePressed(i -> {
                            boolean success = false;
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                                success = true;
                            } else if (panel.isValid()) {
                                panel.closePanel();
                                success = true;
                            }
                            Interactable.playButtonClickSound();
                            return success;
                        }))
                .child(IKey.dynamic(() -> hasFluidFilter() ?
                                getFilterInventory().getStackInSlot(0).getDisplayName() :
                                IKey.lang("metaitem.fluid_filter.name").get())
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }

    public boolean hasFluidFilter() {
        return currentFluidFilter != null;
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxStackSize() > 1 && (isBlacklistFilter() || !hasFluidFilter() || currentFluidFilter.showGlobalTransferLimitSlider());
    }

    public int getMaxTransferSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxStackSize();
        }
        return maxSize;
    }

    public void setMaxTransferSize(int transferStackSize) {
        this.maxSize = MathHelper.clamp(transferStackSize, 1, getMaxStackSize());
        onFilterInstanceChange();
        dirtyNotifiable.markAsDirty();
    }

    public int getMaxStackSize() {
        return hasFluidFilter() ? currentFluidFilter.getMaxTransferSize() : maxFluidSizer.get();
    }

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(getFilterInventory().getStackInSlot(0));
    }

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        var stack = ItemStack.EMPTY;
        try {
            stack = packetBuffer.readItemStack();
        } catch (IOException ignore) {}
        this.filterInventory.setStackInSlot(0, stack);

        if (FilterTypeRegistry.isFluidFilter(stack))
            this.currentFluidFilter = FilterTypeRegistry.getFluidFilterForStack(stack);
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
