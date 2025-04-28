package gtqt.api.util;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class GTQTUtility {
    public static IMultipleTankHandler mergeTankHandlers(List<IMultipleTankHandler> inputFluids,boolean allowSameFluidFillForOutputs) {
        List<IFluidTank> mergedTanks = new ArrayList<>();
        for (IMultipleTankHandler tankHandler : inputFluids) {
            mergedTanks.addAll(tankHandler.getFluidTanks());
        }
        return new FluidTankList(allowSameFluidFillForOutputs, mergedTanks);
    }
    public static boolean isInventoryEmpty(IItemHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return false;
        }

        return true;
    }
    public static boolean isFluidTankListEmpty(FluidTankList inventory) {
        for (int slot = 0; slot < inventory.getTanks(); slot++) {
            if (inventory.getTankAt(slot).getFluid()!=null) return false;
        }

        return true;
    }
}
