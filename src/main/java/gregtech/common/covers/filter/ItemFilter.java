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

public abstract class ItemFilter {

    public static MatchResult<Integer> EMPTY_MATCH = new MatchResult<>(Match.SUCCEED, -1);
    private IDirtyNotifiable dirtyNotifiable;
    private BaseFilterReader filterReader;
    public static final String KEY_ITEMS = "Items";
    public static final String COUNT = "Count";
    public static final String BLACKLIST = "is_blacklist";

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

    public abstract MatchResult<Integer> matchItemStack(ItemStack itemStack);

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

    final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }

    public static <R> MatchResult<R> createResult(Match match, R data) {
        return new MatchResult<>(match, data);
    }

    public static MatchResult<Integer> createResult(Match match, int data) {
        return new MatchResult<>(match, data);
    }

    public static class MatchResult<T> {
        Match match;
        T data;
        private MatchResult(Match match, T data) {
            this.match = match;
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public boolean matched() {
            return match == Match.SUCCEED;
        }

        public void flipMatch() {
            this.match = matched() ?
                    ItemFilter.Match.FAIL :
                    ItemFilter.Match.SUCCEED;
        }
    }

    public enum Match {
        FAIL,
        SUCCEED
    }

    protected static class BaseFilterReader extends ItemStackItemHandler {

        protected final ItemStack container;
        private Supplier<Integer> maxStackSizer = () -> 1;
        private int cache;
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
