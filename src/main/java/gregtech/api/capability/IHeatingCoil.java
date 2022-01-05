package gregtech.api.capability;

/**
 * intended for use in conjunction with {@link gregtech.api.capability.impl.HeatingCoilRecipeLogic}
 * use with temperature-based multiblocks
 */
public interface IHeatingCoil {

    /**
     *
     * @return the current temperature of the multiblock in Kelvin
     */
    int getCurrentTemperature();
}
