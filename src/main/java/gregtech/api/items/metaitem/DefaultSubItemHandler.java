package gregtech.api.items.metaitem;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.common.ConfigHolder;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.List;

public class DefaultSubItemHandler implements ISubItemHandler {

    public static final DefaultSubItemHandler INSTANCE = new DefaultSubItemHandler();

    private DefaultSubItemHandler() {}

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return getFluidContainerSubType(itemStack);
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            electricItem.charge(Long.MAX_VALUE, Integer.MAX_VALUE, true, false);
            subItems.add(itemStack);
        }
        if (creativeTab == CreativeTabs.SEARCH) {
            if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                if (!ConfigHolder.compat.hideFilledCellsInJEI) {
                    addFluidContainerVariants(itemStack, subItems);
                }
            }
        }
    }

    public static String getFluidContainerSubType(ItemStack itemStack) {
        IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY,
                null);
        if (fluidHandler != null) {
            IFluidTankProperties fluidTankProperties = fluidHandler.getTankProperties()[0];
            FluidStack fluid = fluidTankProperties.getContents();
            return String.format("f=%s", fluid == null ? "empty" : fluid.getFluid().getName());
        }
        return "";
    }

    public static void addFluidContainerVariants(ItemStack itemStack, List<ItemStack> subItems) {
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            ItemStack containerStack = itemStack.copy();
            IFluidHandlerItem fluidContainer = containerStack
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidContainer != null) {
                fluidContainer.fill(new FluidStack(fluid, Integer.MAX_VALUE), true);
                if (fluidContainer.drain(Integer.MAX_VALUE, false) == null)
                    continue;
                subItems.add(fluidContainer.getContainer());
            }
        }
    }
}
