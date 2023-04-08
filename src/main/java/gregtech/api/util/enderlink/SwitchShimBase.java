package gregtech.api.util.enderlink;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class SwitchShimBase {
    protected Object container;
    public SwitchShimBase(Object container) {
        changeInventory(container);
    }
    public void changeInventory(Object container) {
        if (container == null) { // i don't think this is necessary anymore?
            throw new IllegalArgumentException("Shim container must be an IItemHandler!");
        }

        if (!(container instanceof IItemHandler || container instanceof IFluidHandler)) {
            throw new IllegalArgumentException("Container must be an IItemHandler or IFluidHandler!");
        }

        this.container = container;
    }
}
