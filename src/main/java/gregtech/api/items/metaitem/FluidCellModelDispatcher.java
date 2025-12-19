package gregtech.api.items.metaitem;

import gregtech.api.items.metaitem.stats.IItemModelDispatcher;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record FluidCellModelDispatcher(int pixel) implements IItemModelDispatcher {

    @Override
    public int getModelIndex(ItemStack itemStack, int maxIndex) {
        var singleStack = itemStack.copy();
        if (singleStack.getCount() > 1) singleStack.setCount(1);

        var handler = FluidUtil.getFluidHandler(singleStack);
        if (handler == null) return pixel;

        var tankProps = handler.getTankProperties();
        if (tankProps.length != 1) return pixel;

        var tankProp = tankProps[0];
        var fluidStack = tankProp.getContents();
        if (fluidStack == null) return pixel;

        boolean isGas = fluidStack.getFluid().isGaseous();
        int amount = fluidStack.amount;
        int capacity = tankProp.getCapacity();

        int fluidLevel = ((pixel - 1) * amount) / capacity;
        return isGas ? 2 * (pixel - 1) - fluidLevel : fluidLevel;
    }
}
