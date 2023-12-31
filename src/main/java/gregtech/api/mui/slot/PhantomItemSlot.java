package gregtech.api.mui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

public class PhantomItemSlot extends ModularSlot {
    protected Supplier<Integer> maxStackSize;
    public static final Supplier<Integer> DEFAULT_SIZE = () -> 64;
    public PhantomItemSlot(IItemHandler itemHandler, int index, Supplier<Integer> maxStackSize) {
        super(itemHandler, index);
        this.maxStackSize = maxStackSize == null ? DEFAULT_SIZE : maxStackSize;
    }

    @Override
    public int getSlotStackLimit() {
        return maxStackSize.get();
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
