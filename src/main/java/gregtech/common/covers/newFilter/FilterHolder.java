package gregtech.common.covers.newFilter;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.ChangeableWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public abstract class FilterHolder<T, F extends Filter<T>> implements INBTSerializable<NBTTagCompound> {

    @Nullable
    private F currentFilter;
    protected final IDirtyNotifiable dirtyNotifiable;
    private FilterMode mode = FilterMode.BOTH;
    protected final IItemHandlerModifiable filterInventory;
    protected final int filterSlotIndex;
    private boolean saveFilterInventory = false;

    protected FilterHolder(IDirtyNotifiable dirtyNotifiable) {
        this(new ItemStackHandler(), 0, dirtyNotifiable);
        this.saveFilterInventory = true;
    }

    protected FilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        this.filterInventory = filterInventory;
        this.filterSlotIndex = filterSlotIndex;
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public Widget createFilterUI(UIBuildContext buildContext) {
        MultiChildWidget widget = new MultiChildWidget();
        ChangeableWidget filterWidget = new ChangeableWidget(() -> currentFilter == null ? null : currentFilter.createFilterUI(buildContext).setDebugLabel("Filter"));
        SlotWidget filterSlot = new SlotWidget(filterInventory, filterSlotIndex);
        filterSlot.setFilter(item -> getFilterOf(item) != null);
        filterSlot.setChangeListener(() -> {
            checkFilter(filterSlot.getMcSlot().getStack());
            filterWidget.notifyChangeServer();
        });

        return widget.addChild(filterSlot.setPos(144, 0))
                .addChild(filterWidget)
                .addChild(new TextWidget(new Text("cover.filter.label").localise())
                        .setTextAlignment(Alignment.CenterLeft)
                        .setPos(1, 0)
                        .setSize(36, 20))
                .setDebugLabel("FilterHolder");
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

    public void onFilterChanged() {
    }

    public void setCurrentFilter(@Nullable F currentFilter) {
        if (this.currentFilter != currentFilter) {
            this.currentFilter = currentFilter;
            if (this.currentFilter != null) {
                this.currentFilter.setDirtyNotifiable(dirtyNotifiable);
            }
            onFilterChanged();
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
            nbt.setTag("FilterInventory", ((ItemStackHandler) filterInventory).serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.mode = FilterMode.values()[nbt.getByte("Mode")];
        if (saveFilterInventory) {
            ((ItemStackHandler) filterInventory).deserializeNBT(nbt.getCompoundTag("FilterInventory"));
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
