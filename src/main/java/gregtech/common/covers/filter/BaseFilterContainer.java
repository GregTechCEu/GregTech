package gregtech.common.covers.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class BaseFilterContainer extends ItemStackHandler {

    private int maxTransferSize = 1;
    private int transferSize;
    private @Nullable BaseFilter currentFilter;
    private @Nullable Runnable onFilterInstanceChange;
    private final IDirtyNotifiable dirtyNotifiable;

    protected BaseFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super();
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public boolean test(Object toTest) {
        return !hasFilter() || getFilter().test(toTest);
    }

    public MatchResult match(Object toMatch) {
        if (!hasFilter())
            return MatchResult.create(true, toMatch, -1);

        return getFilter().match(toMatch);
    }

    public int getTransferLimit(Object stack) {
        if (!hasFilter() || isBlacklistFilter()) {
            return getTransferSize();
        }
        return getFilter().getTransferLimit(stack, getTransferSize());
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
        if (onFilterInstanceChange != null) {
            onFilterInstanceChange.run();
        }
    }

    public void setOnFilterInstanceChange(@Nullable Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public final @NotNull ItemStack getFilterStack() {
        return this.getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (ItemStack.areItemStacksEqual(stack, getFilterStack()))
            return;

        if (stack.isEmpty()) {
            setFilter(null);
        } else if (isItemValid(stack)) {
            setFilter(BaseFilter.getFilterFromStack(stack));
        }

        super.setStackInSlot(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isItemValid(stack);
    }

    protected abstract boolean isItemValid(ItemStack stack);

    protected abstract String getFilterName();

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isItemValid(stack)) return stack;
        var remainder = super.insertItem(slot, stack, simulate);
        if (!simulate) setFilter(BaseFilter.getFilterFromStack(stack));
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = super.extractItem(slot, amount, simulate);
        if (!extracted.isEmpty()) {
            setFilter(null);
        }
        return extracted;
    }

    public final void setFilterStack(ItemStack stack) {
        setStackInSlot(0, stack);
    }

    public int getMaxTransferSize() {
        return !showGlobalTransferLimitSlider() && hasFilter() ? currentFilter.getMaxTransferSize() :
                this.maxTransferSize;
    }

    public void setMaxTransferSize(int maxTransferSize) {
        this.maxTransferSize = MathHelper.clamp(maxTransferSize, 1, Integer.MAX_VALUE);
        this.transferSize = MathHelper.clamp(this.transferSize, 1, this.maxTransferSize);
        if (hasFilter()) currentFilter.setMaxTransferSize(this.maxTransferSize);
    }

    public final boolean hasFilter() {
        return currentFilter != null;
    }

    public final @Nullable BaseFilter getFilter() {
        return currentFilter;
    }

    public final void setFilter(@Nullable BaseFilter newFilter) {
        this.currentFilter = newFilter;
        if (hasFilter()) {
            this.currentFilter.setDirtyNotifiable(this.dirtyNotifiable);
            this.currentFilter.setMaxTransferSize(this.maxTransferSize);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public boolean showGlobalTransferLimitSlider() {
        return this.maxTransferSize > 0 && (!hasFilter() || getFilter().showGlobalTransferLimitSlider());
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasFilter()) getFilter().setBlacklistFilter(blacklistFilter);
        onFilterInstanceChange();
    }

    public final boolean isBlacklistFilter() {
        return hasFilter() && getFilter().isBlacklistFilter();
    }

    public int getTransferSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxTransferSize();
        }
        return this.transferSize;
    }

    public int getTransferLimit(int slotIndex) {
        if (isBlacklistFilter() || !hasFilter()) {
            return getTransferSize();
        }
        return this.currentFilter.getTransferLimit(slotIndex, getTransferSize());
    }

    public void setTransferSize(int transferSize) {
        this.transferSize = MathHelper.clamp(transferSize, 1, getMaxTransferSize());
        onFilterInstanceChange();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("FilterInventory", super.serializeNBT());
        tagCompound.setInteger("TransferStackSize", getTransferSize());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt.getCompoundTag("FilterInventory"));
        setFilter(BaseFilter.getFilterFromStack(getFilterStack()));
        if (nbt.hasKey("TransferStackSize"))
            this.transferSize = nbt.getInteger("TransferStackSize");
    }

    public void handleLegacyNBT(NBTTagCompound nbt) {
        // for filters as covers, the stack is set manually, and "FilterInventory" doesn't exist to be deserialized
        // also, ItemStackHandler's deserialization doesn't use setStackInSlot, so I have to do that manually here
        if (nbt.hasKey("FilterInventory")) {
            super.deserializeNBT(nbt.getCompoundTag("FilterInventory"));
            setFilter(BaseFilter.getFilterFromStack(getFilterStack()));
        }

        if (hasFilter())
            getFilter().getFilterReader().handleLegacyNBT(nbt);
    }

    /** Uses Cleanroom MUI */
    public IWidget initUI(ModularPanel main, PanelSyncManager manager) {
        PanelSyncHandler panel = manager.panel("filter_panel", main, (syncManager, syncHandler) -> {
            var filter = hasFilter() ? getFilter() : BaseFilter.ERROR_FILTER;
            filter.setMaxTransferSize(getMaxTransferSize());
            return filter.createPopupPanel(syncManager);
        });

        var filterButton = new ButtonWidget<>();
        filterButton.setEnabled(hasFilter());

        return new Row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(this, 0)
                                .filter(this::isItemValid)
                                .singletonSlotGroup(101)
                                .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                    if (!isItemValid(newItem) && panel.isPanelOpen()) {
                                        panel.closePanel();
                                    }
                                }))
                        .size(18).marginRight(2)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY.asIcon().size(16)))
                .child(filterButton
                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED,
                                GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .setEnabledIf(w -> hasFilter())
                        .onMousePressed(i -> {
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                            } else {
                                panel.closePanel();
                            }
                            Interactable.playButtonClickSound();
                            return true;
                        }))
                .child(IKey.dynamic(this::getFilterName)
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }

    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeItemStack(this.getFilterStack());
        packetBuffer.writeInt(this.maxTransferSize);
        packetBuffer.writeInt(this.transferSize);
    }

    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        var stack = ItemStack.EMPTY;
        try {
            stack = packetBuffer.readItemStack();
        } catch (IOException ignore) {}
        this.setFilterStack(stack);
        this.setMaxTransferSize(packetBuffer.readInt());
        this.setTransferSize(packetBuffer.readInt());
    }
}
