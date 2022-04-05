package gregtech.common.covers.filter;

import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.Widget;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.api.util.ItemStackKey;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;
import java.util.function.Consumer;

public abstract class ItemFilter {

    private IDirtyNotifiable dirtyNotifiable;
    private int maxStackSize = Integer.MAX_VALUE;

    public final int getMaxStackSize() {
        return maxStackSize;
    }

    public final void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        onMaxStackSizeChange();
    }

    protected void onMaxStackSizeChange() {
    }

    public abstract boolean showGlobalTransferLimitSlider();

    public abstract int getSlotTransferLimit(Object matchSlot, int globalTransferLimit);

    public abstract Object matchItemStack(ItemStack itemStack);

    public abstract int getTotalOccupiedHeight();

    public abstract void initUIOld(Consumer<gregtech.api.guiOld.Widget> widgetGroup);

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
}
