package gregtech.api.util;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static gregtech.api.util.GTUtility.findMachineInBlacklist;

/**
 * This file holds utility methods that are used for specific machines
 */
public class GTMachineUtils {

    /**
     * @param fluidHandler the handler to drain from
     * @param doDrain      if the handler should be actually drained
     * @return a valid boiler fluid from a container, with amount=1
     */
    @Nullable
    public static FluidStack getBoilerFluidFromContainer(@Nonnull IFluidHandler fluidHandler, boolean doDrain) {
        return getBoilerFluidFromContainer(fluidHandler, 1, doDrain);
    }

    /**
     * @param fluidHandler the handler to drain from
     * @param amount       the amount to drain
     * @param doDrain      if the handler should be actually drained
     * @return a valid boiler fluid from a container
     */
    @Nullable
    // TODO, does this method actually need to be in Util?
    public static FluidStack getBoilerFluidFromContainer(@Nonnull IFluidHandler fluidHandler, int amount, boolean doDrain) {
        if (amount == 0) return null;
        FluidStack drainedWater = fluidHandler.drain(Materials.Water.getFluid(amount), doDrain);
        if (drainedWater == null || drainedWater.amount == 0) {
            drainedWater = fluidHandler.drain(Materials.DistilledWater.getFluid(amount), doDrain);
        }
        if (drainedWater == null || drainedWater.amount == 0) {
            for (String fluidName : ConfigHolder.machines.boilerFluids) {
                Fluid fluid = FluidRegistry.getFluid(fluidName);
                if (fluid != null) {
                    drainedWater = fluidHandler.drain(new FluidStack(fluid, amount), doDrain);
                    if (drainedWater != null && drainedWater.amount > 0) {
                        break;
                    }
                }
            }
        }
        return drainedWater;
    }

    /**
     * Checks whether a machine is not a multiblock and has a recipemap not present in a blacklist
     *
     * @param machineStack the ItemStack containing the machine to check the validity of
     * @return whether the machine is valid or not
     */
    public static boolean isMachineValidForMachineHatch(ItemStack machineStack, String[] recipeMapBlacklist) {

        if (machineStack == null || machineStack.isEmpty()) {
            return false;
        }

        MetaTileEntity machine = GTUtility.getMetaTileEntity(machineStack);
        if (machine instanceof WorkableTieredMetaTileEntity && !(machine instanceof SimpleGeneratorMetaTileEntity)) {
            RecipeMap<?> recipeMap = machine.getRecipeMap();
            return recipeMap != null && !findMachineInBlacklist(recipeMap.getUnlocalizedName(), recipeMapBlacklist);
        }

        return false;
    }
}
