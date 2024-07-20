package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidState;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidStorageKeys {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(gregtechId("liquid"),
            MaterialIconType.liquid,
            m -> prefixedRegistryName("liquid.", FluidStorageKeys.LIQUID, m),
            m -> m.hasProperty(PropertyKey.DUST) ? "gregtech.fluid.liquid_generic" : "gregtech.fluid.generic",
            FluidState.LIQUID);

    public static final FluidStorageKey GAS = new FluidStorageKey(gregtechId("gas"),
            MaterialIconType.gas,
            m -> prefixedRegistryName("gas.", FluidStorageKeys.GAS, m),
            m -> {
                if (m.hasProperty(PropertyKey.DUST)) {
                    return "gregtech.fluid.gas_vapor";
                }

                FluidProperty property = m.getProperty(PropertyKey.FLUID);
                if (m.isElement() || property == null || property.getPrimaryKey() != FluidStorageKeys.GAS) {
                    return "gregtech.fluid.gas_generic";
                }
                return "gregtech.fluid.generic";
            },
            FluidState.GAS);

    public static final FluidStorageKey PLASMA = new FluidStorageKey(gregtechId("plasma"),
            MaterialIconType.plasma,
            m -> "plasma." + m.getName(),
            m -> "gregtech.fluid.plasma",
            FluidState.PLASMA, -1);

    private FluidStorageKeys() {}

    /**
     * Used to create registry names for fluids that only have a prefix when not stored by the primary key.
     *
     * @param prefix   the prefix string for the registry name
     * @param key      the key which does not require the prefix
     * @param material the material to create a registry name for
     * @return the registry name
     */
    public static @NotNull String prefixedRegistryName(@NotNull String prefix, @NotNull FluidStorageKey key,
                                                       @NotNull Material material) {
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property != null && property.getPrimaryKey() != key) {
            return prefix + material.getName();
        }
        return material.getName();
    }
}
