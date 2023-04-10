package gregtech.api.util.enderlink;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class SwitchShimBase<T> {
    protected T container;
    public SwitchShimBase(T container) {
        changeInventory(container);
    }
    public void changeInventory(T container) {
        if (container == null) { // i don't think this is necessary anymore?
            throw new IllegalArgumentException("Shim container must be an IItemHandler!");
        }

        if (!(container instanceof IItemHandler || container instanceof IFluidHandler)) {
            throw new IllegalArgumentException("Container must be an IItemHandler or IFluidHandler!");
        }

        this.container = container;
    }
}
