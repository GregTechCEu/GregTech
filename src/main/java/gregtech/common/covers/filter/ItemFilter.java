package gregtech.common.covers.filter;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.readers.BaseFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemFilter implements Filter<ItemStack> {

    private IDirtyNotifiable dirtyNotifiable;
    private BaseItemFilterReader filterReader;

    private OnMatch<ItemStack> onMatch = null;

    protected void setFilterReader(BaseItemFilterReader reader) {
        this.filterReader = reader;
    }

    public ItemStack getContainerStack() {
        var stack = this.filterReader.getContainer();
        stack.setCount(1);
        return stack;
    }

    public final void setBlacklistFilter(boolean blacklistFilter) {
        this.filterReader.setBlacklistFilter(blacklistFilter);
        markDirty();
    }

    public final boolean isBlacklistFilter() {
        return filterReader.isBlacklistFilter();
    }

    public final int getMaxStackSize() {
        return this.filterReader.getMaxStackSize();
    }

    public final void setMaxStackSize(int maxStackSize) {
        setMaxStackSizer(() -> maxStackSize);
    }

    public final void setMaxStackSizer(Supplier<Integer> maxStackSizer) {
        this.filterReader.setMaxStackSizer(maxStackSizer);
    }

    public Supplier<Integer> getMaxStackSizer() {
        return this.filterReader.getMaxStackSizer();
    }

    public final void onMaxStackSizeChange() {
        this.filterReader.onMaxStackSizeChange();
    }

    public abstract boolean showGlobalTransferLimitSlider();

    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        return 0;
    }

    public int getStackTransferLimit(ItemStack stack, int globalTransferLimit) {
        return 0;
    }

    /** Deprecated, uses old builtin MUI */
    @Deprecated
    public abstract void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup);

    /** Uses Cleanroom MUI */
    public abstract @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI */
    public abstract @NotNull ModularPanel createPanel(GuiSyncManager syncManager);

    /** Uses Cleanroom MUI - Creates the widgets standalone so that they can be put into their own panel */

    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        var blacklist = new BooleanSyncValue(this.filterReader::isBlacklistFilter, this.filterReader::setBlacklistFilter);
        return new ParentWidget<>().coverChildren()
                .child(new CycleButtonWidget()
                .value(blacklist)
                .textureGetter(state -> GTGuiTextures.BUTTON_BLACKLIST[state])
                .addTooltip(0, IKey.lang("cover.filter.blacklist.disabled"))
                .addTooltip(1, IKey.lang("cover.filter.blacklist.enabled")));
    }

    @Deprecated
    public void readFromNBT(NBTTagCompound tagCompound) {
        setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        markDirty();
    }

    public abstract void match(ItemStack itemStack);

    protected final void onMatch(boolean matched, ItemStack stack, int matchSlot) {
        if (this.onMatch != null) this.onMatch.onMatch(matched, stack, matchSlot);
    }

    public abstract boolean test(ItemStack toTest);

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    @Override
    public void setOnMatched(OnMatch<ItemStack> onMatch) {
        this.onMatch = onMatch;
    }

    protected static class BaseItemFilterReader extends BaseFilterReader {
        private Supplier<Integer> maxStackSizer = () -> 1;
        private int cache;
        protected static final String COUNT = "Count";
        public BaseItemFilterReader(ItemStack container, int slots) {
            super(container, slots);
            setBlacklistFilter(false);
        }

        public void onMaxStackSizeChange() {
            this.cache = getMaxStackSizer().get();
        }

        public final int getMaxStackSize() {
            return this.isBlacklistFilter() ? 1 : this.cache;
        }

        public final void setMaxStackSizer(Supplier<Integer> maxStackSizer) {
            this.maxStackSizer = maxStackSizer;
            this.cache = this.maxStackSizer.get();
        }

        public Supplier<Integer> getMaxStackSizer() {
            return this.maxStackSizer;
        }
    }
}
