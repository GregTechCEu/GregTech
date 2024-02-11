package gregtech.common.mui.widget.orefilter;

import gregtech.api.util.oreglob.OreGlob;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemOreFilterTestSlot extends ModularSlot {

    OreFilterTestSlot parent;

    OreGlob glob;

    public ItemOreFilterTestSlot() {
        super(new ItemStackHandler(1), 0, true);
    }

    void setParent(OreFilterTestSlot parent) {
        this.parent = parent;
    }

    public void setGlob(@Nullable OreGlob glob) {
        if (this.glob == glob) return;
        this.glob = glob;
    }

    @Override
    public void putStack(ItemStack stack) {
        ItemStack testStack = getStack();
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !testStack.isItemEqual(stack) ||
                !ItemStack.areItemStackTagsEqual(testStack, stack)) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            super.putStack(stack);
            this.parent.updatePreview();
        }
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return 1;
    }
}
