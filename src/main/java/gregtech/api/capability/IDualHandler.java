package gregtech.api.capability;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public interface IDualHandler extends IItemHandler, IFluidHandler {

    boolean hasFluidTanks();

    boolean hasItemHandlers();
}
