package gregtech.common.covers.filter;

import com.cleanroommc.modularui.utils.ItemStackItemHandler;

import com.cleanroommc.modularui.widget.ParentWidget;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;

import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemFilter implements Filter<ItemStack> {

    private IDirtyNotifiable dirtyNotifiable;
    private BaseFilterReader filterReader;

    private OnMatch<ItemStack> onMatch = null;

    protected void setFilterReader(BaseFilterReader reader) {
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

    public abstract @NotNull ParentWidget<?> createWidgets(GuiSyncManager syncManager);

    public void writeToNBT(NBTTagCompound tagCompound) {
//        tagCompound.setBoolean("IsBlacklist", this.isBlacklistFilter);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
//        this.isBlacklistFilter = tagCompound.getBoolean("IsBlacklist");
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

    protected static class BaseFilterReader extends ItemStackItemHandler {

        protected final ItemStack container;
        private Supplier<Integer> maxStackSizer = () -> 1;
        private int cache;
        protected static final String KEY_ITEMS = "Items";
        protected static final String COUNT = "Count";
        protected static final String BLACKLIST = "is_blacklist";
        public BaseFilterReader(ItemStack container, int slots) {
            super(container, slots);
            this.container = container;
            setBlacklistFilter(false);
        }

        public ItemStack getContainer () {
            return this.container;
        }

        public void onMaxStackSizeChange() {
            this.cache = getMaxStackSizer().get();
        }

        public final void setBlacklistFilter(boolean blacklistFilter) {
            getStackTag().setBoolean(BLACKLIST, blacklistFilter);
            onMaxStackSizeChange();
        }

        public final boolean isBlacklistFilter() {
            return getStackTag().getBoolean(BLACKLIST);
        }

        protected NBTTagCompound getStackTag() {
            if (!container.hasTagCompound()) {
                container.setTagCompound(new NBTTagCompound());
            }
            return container.getTagCompound();
        }

        @Override
        public NBTTagList getItemsNbt() {
            NBTTagCompound nbt = getStackTag();
            if (!nbt.hasKey(KEY_ITEMS)) {
                NBTTagList list = new NBTTagList();
                for (int i = 0; i < getSlots(); i++) {
                    list.appendTag(new NBTTagCompound());
                }
                nbt.setTag(KEY_ITEMS, list);
            }
            return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
        }

        @Override
        protected void validateSlotIndex(int slot) {
            if (slot < 0 || slot >= this.getSlots()) {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
            }
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
