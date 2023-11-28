package gregtech.api.gui.widgets;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FluidContainerSlotWidget extends SlotWidget {

    private final boolean requireFilledContainer;

    public FluidContainerSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                                    boolean requireFilledContainer) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
        this.requireFilledContainer = requireFilledContainer;
    }

    @Override
    public boolean canPutStack(ItemStack stack) {
        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(stack);
        return fluidHandlerItem != null &&
                (!requireFilledContainer || fluidHandlerItem.getTankProperties()[0].getContents() != null);
    }
}
