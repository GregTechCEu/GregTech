package gregtech.api.block;

import org.jetbrains.annotations.NotNull;

/**
 * Implement this interface on the Block Enum for your Heating Coil block
 *
 * @see gregtech.common.blocks.BlockWireCoil.CoilType
 */
public interface IRefractoryBrickBlockStats {

    /**
     * @return The Unique Name of the Heating Coil
     */
    @NotNull
    String getName();

    /**
     * @return the temperature the Heating Coil provides
     */
    int getRefractoryBrickTemperature();

    /**
     * This is used for the amount of parallel recipes in the multi smelter
     *
     * @return the level of the Heating Coil
     */
    int getLevel();

    /**
     * This is used for the energy discount in the multi smelter
     *
     * @return the energy discount of the Heating Coil
     */
    int getEnergyDiscount();

    /**
     * This is used for the energy discount in the cracking unit and pyrolyse oven
     *
     * @return the tier of the coil
     */
    int getTier();


}
