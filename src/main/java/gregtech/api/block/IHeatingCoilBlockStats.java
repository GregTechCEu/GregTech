package gregtech.api.block;

import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.unification.material.Material;
import gregtech.client.model.ActiveVariantBlockBakedModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Implement this interface on the Block Enum for your Heating Coil block
 *
 * @see gregtech.common.blocks.BlockWireCoil.CoilType
 */
public interface IHeatingCoilBlockStats {

    /**
     * @return The Unique Name of the Heating Coil
     */
    @NotNull
    String getName();

    /**
     * @return the temperature the Heating Coil provides
     */
    int getCoilTemperature();

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

    /**
     * Used for {@link TemperatureProperty#registerCoilType(int, Material, String)}
     *
     * @return the {@link Material} of the Heating Coil if it has one, otherwise {@code null}
     */
    @Nullable
    Material getMaterial();

    default int getColor() {
        return getMaterial() == null ? 0xFFFFFFFF : getMaterial().getMaterialRGB();
    }

    default ActiveVariantBlockBakedModel createModel(BooleanSupplier bloomConfig) {
        return null;
    }
}
