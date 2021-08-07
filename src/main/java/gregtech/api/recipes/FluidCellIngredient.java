package gregtech.api.recipes;

import gregtech.api.unification.material.Material;
import gregtech.api.util.GTLog;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class FluidCellIngredient extends Ingredient {

    Fluid fluid;

    /**
     * @param fluid
     * @param count Set to 0 for non consumable
     * @return
     */
    public static CountableIngredient getIngredient(Fluid fluid, int count) {
        return new CountableIngredient(new FluidCellIngredient(fluid), count);
    }

    public static CountableIngredient getIngredient(Material material, int count) {
        return new CountableIngredient(new FluidCellIngredient(material.getFluid()), count);
    }

    public FluidCellIngredient(Fluid fluid) {
        super(getFilledCell(fluid, 1));
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
    public boolean apply(@Nullable ItemStack itemStack) {
        IFluidHandlerItem stackFluid = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack drained = stackFluid == null ? null : stackFluid.getTankProperties()[0].getContents();
        return itemStack != null && MetaItems.FLUID_CELL.isItemEqual(itemStack) && drained != null && drained.getFluid() == fluid && drained.amount == 1000;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

}
