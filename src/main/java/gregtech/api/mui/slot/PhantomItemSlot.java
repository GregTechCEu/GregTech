package gregtech.api.mui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

public class PhantomItemSlot extends ModularSlot {
    protected Supplier<Integer> maxStackSizer;
    public PhantomItemSlot(IItemHandler itemHandler, int index, Supplier<Integer> maxStackSizer) {
        super(itemHandler, index);
        this.maxStackSizer = maxStackSizer;
    }

    @Override
    public int getSlotStackLimit() {
        return maxStackSizer.get();
    }

    @Override
    public boolean isPhantom() {
        return true;
    }

    @Override
    public boolean isIgnoreMaxStackSize() {
        return true;
    }
}
