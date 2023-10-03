package gregtech.api.cover.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.newgui.FilterPanelSyncHandler;
import gregtech.api.newgui.GTGuis;
import gregtech.api.newgui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public abstract class FilterHolder<T, F extends Filter<T>> implements INBTSerializable<NBTTagCompound> {

    @Nullable
    private F currentFilter;
    protected final IDirtyNotifiable dirtyNotifiable;
    private FilterMode mode = FilterMode.BOTH;
    protected final IItemHandlerModifiable filterInventory;
    protected final int filterSlotIndex;
    private final boolean saveFilterInventory;

    protected FilterHolder(IDirtyNotifiable dirtyNotifiable) {
        this(new ItemStackHandler() {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        }, 0, dirtyNotifiable);
    }

    protected FilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        this.filterInventory = filterInventory;
        this.filterSlotIndex = filterSlotIndex;
        this.dirtyNotifiable = dirtyNotifiable;
        this.saveFilterInventory = filterInventory instanceof INBTSerializable<?>;
    }

    public IWidget createFilterUI(ModularPanel mainPanel, GuiCreationContext creationContext, GuiSyncManager syncManager) {
        ButtonWidget<?> openFilterConfigButton = new ButtonWidget<>();
        FilterPanelSyncHandler filterPanelSyncHandler = new FilterPanelSyncHandler(mainPanel, this);
        syncManager.syncValue("filter_config", filterPanelSyncHandler);
        return new ParentWidget<>()
                .height(18).widthRel(1f)
                .child(IKey.lang("cover.filter.label").asWidget().height(18).left(0))
                .child(new ItemSlot()
                        .slot(new ModularSlot(this.filterInventory, this.filterSlotIndex)
                                .filter(item -> getFilterOf(item) != null)
                                .changeListener((stack, onlyAmountChanged, client, init) -> {
                                    checkFilter(stack);
                                    if (!init && client) {
                                        openFilterConfigButton.setEnabled(hasFilter());
                                        if (!hasFilter() || filterPanelSyncHandler.isPanelOpen()) {
                                            filterPanelSyncHandler.closePanel();
                                        } else {
                                            filterPanelSyncHandler.openPanel();
                                        }
                                    }
                                }))
                        .pos(60, 0))
                .child(openFilterConfigButton
                        .onMousePressed(mouseButton -> {
                            if (hasFilter() && !filterPanelSyncHandler.isPanelOpen()) {
                                filterPanelSyncHandler.openPanel();
                            }
                            return true;
                        })
                        .overlay(IKey.lang("cover.filter.settings_open.label"))
                        .pos(80, 0)
                        .size(79, 18));
    }

    public ModularPanel buildFilterPanel(ModularPanel mainPanel, GuiSyncManager syncManager, FilterPanelSyncHandler filterPanelSyncHandler) {
        ModularPanel panel;
        if (!hasFilter()) {
            return (panel = GTGuis.createPanel("error", 32, 30))
                    .background(GuiTextures.BACKGROUND)
                    .child(IKey.lang("An Error occurred!").color(Color.RED.normal).asWidget()
                            .size(130, 20))
                    .child(new ButtonWidget<>()
                            .onMousePressed(mouseButton -> {
                                filterPanelSyncHandler.closePanel();
                                return true;
                            })
                            .pos(73, 4));
        }

        IWidget filterUI = this.currentFilter.createFilterUI(mainPanel, syncManager);
        filterUI.flex().pos(5, 20).margin(5);
        panel = new ModularPanel("filter_config")
                .coverChildren()
                .relative(mainPanel)
                .top(0)
                .rightRel(1f)
                .padding(5)
                .child(IKey.lang("cover.filter.settings.label").asWidget().pos(5, 5));
        panel.child(new ButtonWidget<>()
                        .overlay(GuiTextures.CROSS)
                        .onMousePressed(mouseButton -> {
                            filterPanelSyncHandler.closePanel();
                            return true;
                        }).top(5).right(5).size(10))
                .child(filterUI);
        //int height = filterUI.getSize().height > 0 ? filterUI.getSize().height + 25 : 90;
        //int width = filterUI.getSize().width > 0 ? filterUI.getSize().width + 10 : 150;
        return panel;
    }

    @Nullable
    public F getFilterOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Filter<?> filter = FilterRegistry.createFilter(stack);
        if (filter == null) {
            return null;
        }
        if (getFilterClass().isAssignableFrom(filter.getClass())) {
            return (F) filter;
        }
        return null;
    }

    public abstract Class<F> getFilterClass();

    public void checkFilter(ItemStack itemStack) {
        F filter = getFilterOf(itemStack);
        if (currentFilter == null ^ filter == null || (currentFilter != null && currentFilter.getClass() != filter.getClass())) {
            setCurrentFilter(filter);
        }
    }

    public void onFilterChanged(@Nullable F oldFilter, @Nullable F newFilter) {
    }

    public void setCurrentFilter(@Nullable F currentFilter) {
        if (this.currentFilter != currentFilter) {
            F oldFilter = this.currentFilter;
            this.currentFilter = currentFilter;
            if (this.currentFilter != null) {
                this.currentFilter.setDirtyNotifiable(dirtyNotifiable);
            }
            onFilterChanged(oldFilter, this.currentFilter);
        }
    }

    @Nullable
    public F getCurrentFilter() {
        return currentFilter;
    }

    public boolean hasFilter() {
        return currentFilter != null;
    }

    public void setFilterMode(FilterMode mode) {
        this.mode = mode;
    }

    public FilterMode getFilterMode() {
        return mode;
    }

    public IItemHandlerModifiable getFilterInventory() {
        return filterInventory;
    }

    public boolean test(T t, boolean ignoresInverted) {
        return currentFilter == null || currentFilter.matches(t, ignoresInverted);
    }

    public boolean test(T t) {
        return currentFilter == null || currentFilter.matches(t);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Mode", (byte) mode.ordinal());
        if (currentFilter != null) {
            NBTTagCompound filterInventory = new NBTTagCompound();
            currentFilter.writeToNBT(filterInventory);
            nbt.setTag("Filter", filterInventory);
        }
        if (saveFilterInventory) {
            nbt.setTag("FilterInventory", ((INBTSerializable<NBTTagCompound>) filterInventory).serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.mode = FilterMode.values()[nbt.getByte("Mode")];
        if (saveFilterInventory) {
            ((INBTSerializable<NBTTagCompound>) filterInventory).deserializeNBT(nbt.getCompoundTag("FilterInventory"));
        }
        if (currentFilter == null) {
            checkFilter(filterInventory.getStackInSlot(filterSlotIndex));
        }
        if (currentFilter != null) {
            this.currentFilter.readFromNBT(nbt.getCompoundTag("Filter"));
            if (nbt.hasKey("IsBlacklist")) {
                this.currentFilter.setInverted(nbt.getBoolean("IsBlacklist"));
            }
        }
    }
}
