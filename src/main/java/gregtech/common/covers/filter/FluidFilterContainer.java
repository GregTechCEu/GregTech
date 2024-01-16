package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
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

public class FluidFilterContainer extends BaseFilterContainer<FluidStack, FluidFilter> implements INBTSerializable<NBTTagCompound> {

    private final FluidFilterWrapper filterWrapper;

    public FluidFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
        this.filterWrapper = new FluidFilterWrapper(this); // for compat
    }

    public FluidFilterWrapper getFilterWrapper() {
        return filterWrapper;
    }

    public boolean testFluidStack(FluidStack fluidStack) {
        return testFluidStack(fluidStack, !isBlacklistFilter());
    }

    public boolean testFluidStack(FluidStack fluidStack, boolean whitelist) {
        boolean result = true;
        if (hasFilter()) {
            result = getFilter().test(fluidStack);
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
        widgetGroup.accept(new WidgetGroupFluidFilter(y, this::getFilter, this::showGlobalTransferLimitSlider));
    }

    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        gregtech.api.gui.widgets.ServerWidgetGroup blacklistButton = new gregtech.api.gui.widgets.ServerWidgetGroup(this::hasFilter);
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
                getFilter().setMaxTransferSize(getMaxTransferSize());
                return getFilter().createPopupPanel(syncManager);
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
                            if (!hasFilter() && panel.isPanelOpen()) {
                                panel.closePanel();
                            }
                        }, true)
                        .size(18).marginRight(4)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY))
                .child(new ButtonWidget<>()
                        .setEnabledIf(w -> hasFilter())
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
                .child(IKey.dynamic(() -> hasFilter() ?
                                getFilterInventory().getStackInSlot(0).getDisplayName() :
                                IKey.lang("metaitem.fluid_filter.name").get())
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        var stack = ItemStack.EMPTY;
        try {
            stack = packetBuffer.readItemStack();
        } catch (IOException ignore) {}
        this.filterInventory.setStackInSlot(0, stack);

        if (FilterTypeRegistry.isFluidFilter(stack))
            setFilter(FilterTypeRegistry.getFluidFilterForStack(stack));
    }

    protected void onFilterSlotChange(boolean notify) {
        ItemStack filterStack = filterInventory.getStackInSlot(0);
        int newId = FilterTypeRegistry.getFilterIdForStack(filterStack);
        int currentId = FilterTypeRegistry.getIdForFilter(getFilter());

        if (!FilterTypeRegistry.isFluidFilter(filterStack)) {
            if (hasFilter()) {
                setFilter(null);
                setBlacklistFilter(false);
                if (notify)
                    onFilterInstanceChange();
            }
        } else if (currentId == -1 || newId != currentId) {
            setFilter(FilterTypeRegistry.getFluidFilterForStack(filterStack));
            if (notify)
                onFilterInstanceChange();
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        super.deserializeNBT(tagCompound);
        var stack = getFilterInventory().getStackInSlot(0);
        if (FilterTypeRegistry.isFluidFilter(stack)) {
            setFilter(FilterTypeRegistry.getFluidFilterForStack(stack));
            this.getFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
        }
    }
}
