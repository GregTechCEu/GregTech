package gregtech.common.covers.filter;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ItemFilter {

    public static MatchResult<Integer> EMPTY_MATCH = new MatchResult<>(Match.SUCCEED, -1);
    private IDirtyNotifiable dirtyNotifiable;
    private int maxStackSize = Integer.MAX_VALUE;
    protected boolean isBlacklistFilter = false;

    public final int getMaxStackSize() {
        return maxStackSize;
    }

    public final void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        onMaxStackSizeChange();
    }

    protected void onMaxStackSizeChange() {}

    public abstract boolean showGlobalTransferLimitSlider();

    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        return 0;
    }

    public int getStackTransferLimit(ItemStack stack, int globalTransferLimit) {
        return 0;
    }

    public abstract MatchResult<Integer> matchItemStack(ItemStack itemStack);

    public abstract int getTotalOccupiedHeight();

    /** Deprecated, uses old builtin MUI */
    @Deprecated
    public abstract void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup);

    /** Uses Cleanroom MUI */
    public abstract @NotNull ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager);

    public abstract void writeToNBT(NBTTagCompound tagCompound);

    public abstract void readFromNBT(NBTTagCompound tagCompound);

    final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }
    public final void setBlacklistFilter(boolean blacklistFilter) {
        isBlacklistFilter = blacklistFilter;
        markDirty();
    }

    public final boolean isBlacklistFilter() {
        return isBlacklistFilter;
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
}
