package gregtech.api.recipes;

import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.util.GTLog;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class FluidCellInput extends GTRecipeItemInput {

    final Fluid fluid;

    public FluidCellInput(Fluid fluid) {
        super(getFilledCell(fluid));
        this.fluid = fluid;
    }

    public static ItemStack getFilledCell(Fluid fluid, int count) {
        ItemStack fluidCell = MetaItems.FLUID_CELL.getStackForm().copy();
        IFluidHandlerItem fluidHandlerItem = fluidCell.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        try {
            fluidHandlerItem.fill(new FluidStack(fluid, 1000), true);

        } catch (Exception e) {
            GTLog.logger.error("The fluid " + fluid.toString() + " failed to do something with getFilledCell");
            GTLog.logger.error(e);
            fluidHandlerItem.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
        }
        fluidCell = fluidHandlerItem.getContainer();
        fluidCell.setCount(count);
        return fluidCell;
    }

    public static ItemStack getFilledCell(Fluid fluid) {
        return getFilledCell(fluid, 1);
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        IFluidHandlerItem stackFluid = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack drained = stackFluid == null ? null : stackFluid.getTankProperties()[0].getContents();
        return MetaItems.FLUID_CELL.isItemEqual(itemStack) && drained != null && drained.getFluid() == fluid && drained.amount == 1000;
    }

}
