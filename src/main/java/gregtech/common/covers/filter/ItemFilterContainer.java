package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemFilterContainer implements INBTSerializable<NBTTagCompound> {

    private final ItemStackHandler filterInventory;
    private final ItemFilterWrapper filterWrapper;
    private final IDirtyNotifiable dirtyNotifiable;
    private int maxStackSize = 1;
    private Supplier<Integer> stackSizer = () -> this.maxStackSize;
    private ItemFilter currentItemFilter;
    private Runnable onFilterInstanceChange;
    private int transferStackSize;

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this.filterWrapper = new ItemFilterWrapper(this); // for compat
        this.dirtyNotifiable = dirtyNotifiable;
        this.filterInventory = new ItemStackHandler(1) {

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return isFilter(stack);
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
    }

    public ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    @Deprecated
    public ItemFilterWrapper getFilterWrapper() {
        return filterWrapper;
    }

    public int getMaxStackSize() {
        return hasItemFilter() ? currentItemFilter.getMaxStackSize() : 1;
    }

    public int getTransferStackSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxStackSize();
        }
        return transferStackSize;
    }

    public void setTransferStackSize(int transferStackSize) {
        this.transferStackSize = MathHelper.clamp(transferStackSize, 1, getMaxStackSize());
        onFilterInstanceChange();
        dirtyNotifiable.markAsDirty();
    }

    public void adjustTransferStackSize(int amount) {
        setTransferStackSize(transferStackSize + amount);
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasItemFilter()) getItemFilter().setBlacklistFilter(blacklistFilter);
        onFilterInstanceChange();
    }

    public boolean isBlacklistFilter() {
        return hasItemFilter() && getItemFilter().isBlacklistFilter();
    }

    /** Deprecated, uses old builtin MUI*/
    @Deprecated
    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(filterInventory, 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 38, widgetGroup);
    }
    /** Deprecated, uses old builtin MUI*/
    @Deprecated
    public void initFilterUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getItemFilter));
    }
    /** Deprecated, uses old builtin MUI*/
    @Deprecated
    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(() -> getItemFilter() != null);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 20, 20, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    /** Uses Cleanroom MUI*/
    public IWidget initUI(ModularPanel main, GuiSyncManager manager) {
        var slotGroup = new SlotGroup("filter_slot", 1, true);
        manager.registerSlotGroup(slotGroup);
        var panel = new PanelSyncHandler(main) {
            @Override
            public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
                getItemFilter().setMaxStackSizer(stackSizer);
                return getItemFilter().createUI(mainPanel, syncManager);
            }
        };
        manager.syncValue("filter_panel", panel);

        return new Row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(filterInventory, 0)
                                .slotGroup(slotGroup)
                                .filter(this::isFilter))
                        .size(18)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY))
                .child(new ButtonWidget<>()
                        .setEnabledIf(w -> hasItemFilter())
                        .onMousePressed(i -> {
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                                return true;
                            } else if (panel.isValid()) {
                                panel.closePanel();
                                return true;
                            }
                            return false;
                        }))
                .child(IKey.dynamic(() -> hasItemFilter() ?
                                getFilterInventory().getStackInSlot(0).getDisplayName() :
                                IKey.lang("cover.conveyor.item_filter.title").get())
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }

    protected void onFilterSlotChange(boolean notify) {
        ItemStack filterStack = filterInventory.getStackInSlot(0);
        ItemFilter newItemFilter = FilterTypeRegistry.getItemFilterForStack(filterStack);
        ItemFilter currentItemFilter = getItemFilter();
        if (newItemFilter == null) {
            if (hasItemFilter()) {
                setItemFilter(null);
                setBlacklistFilter(false);
                if (notify) onFilterInstanceChange();
            }
        } else if (currentItemFilter == null ||
                newItemFilter.getClass() != currentItemFilter.getClass()) {
                    setItemFilter(newItemFilter);
                    if (notify) onFilterInstanceChange();
        }
    }

    public void setOnFilterInstanceChange(Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public void onFilterInstanceChange() {
        this.maxStackSize = isBlacklistFilter() ? 1 : getMaxStackSize();
        dirtyNotifiable.markAsDirty();
    }

    public void setMaxStackSize(int maxStackSizeLimit) {
        this.maxStackSize = maxStackSizeLimit;
        if (hasItemFilter() && !isBlacklistFilter()) {
            setFilterStackSizer(() -> this.maxStackSize);
        }
    }

    public void setFilterStackSizer(Supplier<Integer> stackSizer) {
        this.stackSizer = stackSizer;
    }
    public boolean showGlobalTransferLimitSlider() {
        return getMaxStackSize() > 1 && (isBlacklistFilter() || !hasItemFilter()|| currentItemFilter.showGlobalTransferLimitSlider());
    }

    public int getSlotTransferLimit(int slotIndex) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return getTransferStackSize();
        }
        return currentItemFilter.getSlotTransferLimit(slotIndex, getTransferStackSize());
    }

    public int getStackTransferLimit(ItemStack stack) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return getTransferStackSize();
        }
        return currentItemFilter.getStackTransferLimit(stack, getTransferStackSize());
    }

    public ItemFilter.MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        ItemFilter.MatchResult<Integer> originalResult;
        if (currentItemFilter == null) {
            originalResult = ItemFilter.EMPTY_MATCH;
        } else {
            originalResult = currentItemFilter.matchItemStack(itemStack);
        }
        return originalResult;
    }

    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack).matched();
    }


    public void setItemFilter(ItemFilter itemFilter) {
        this.currentItemFilter = itemFilter;
        if (currentItemFilter != null) {
            currentItemFilter.setDirtyNotifiable(dirtyNotifiable);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public ItemFilter getItemFilter() {
        return currentItemFilter;
    }

    public boolean hasItemFilter() {
        return currentItemFilter != null;
    }

    public boolean isFilter(ItemStack stack) {
        return FilterTypeRegistry.getItemFilterForStack(stack) != null;
    }

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(getFilterInventory().getStackInSlot(0));
    }

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        try {
            var stack = packetBuffer.readItemStack();
            this.filterInventory.setStackInSlot(0, stack);
            this.currentItemFilter = FilterTypeRegistry.getItemFilterForStack(stack);
        } catch (IOException ignore) {}
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("FilterInventory", filterInventory.serializeNBT());
        tagCompound.setInteger("MaxStackSize", maxStackSize);
        tagCompound.setInteger("TransferStackSize", transferStackSize);
        if (getItemFilter() != null) {
            NBTTagCompound filterInventory = new NBTTagCompound();
            getItemFilter().writeToNBT(filterInventory);
            tagCompound.setTag("Filter", filterInventory);
        }
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.filterInventory.deserializeNBT(tagCompound.getCompoundTag("FilterInventory"));
        this.currentItemFilter = FilterTypeRegistry.getItemFilterForStack(getFilterInventory().getStackInSlot(0));
        this.maxStackSize = tagCompound.getInteger("MaxStackSize");
        this.transferStackSize = tagCompound.getInteger("TransferStackSize");
        if (getItemFilter() != null) {
            this.getItemFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
        }
    }
}
