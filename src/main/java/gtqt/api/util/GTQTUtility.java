package gtqt.api.util;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;

import net.minecraftforge.fluids.IFluidTank;

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
}
