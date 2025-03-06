package gregtech.api.capability;

/**
 * intended for use in conjunction with
 * {@link gregtech.api.recipes.logic.statemachine.overclock.RecipeCoilOverclockingOperator}
 * in temperature-based multiblocks
 */
public interface IHeatingCoil {

    /**
     *
     * @return the current temperature of the multiblock in Kelvin
     */
    int getCurrentTemperature();
}
