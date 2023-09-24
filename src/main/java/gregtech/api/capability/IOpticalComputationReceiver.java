package gregtech.api.capability;

import gregtech.api.capability.impl.ComputationRecipeLogic;

/**
 * Used in conjunction with {@link ComputationRecipeLogic}.
 */
public interface IOpticalComputationReceiver {

    IOpticalComputationProvider getComputationProvider();
}
