package gregtech.api.fluids.store;

import gregtech.api.fluids.FluidState;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidStorageKeys {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(gregtechId("liquid"),
            MaterialIconType.liquid,
            m -> {
                FluidProperty property = m.getProperty(PropertyKey.FLUID);
                if (property != null && property.getPrimaryKey() != FluidStorageKeys.LIQUID) {
                    return "liquid." + m.getName();
                }
                return m.getName();
            },
            m -> m.hasProperty(PropertyKey.DUST) ? "gregtech.fluid.liquid_generic" : "gregtech.fluid.generic",
            FluidState.LIQUID);

    public static final FluidStorageKey GAS = new FluidStorageKey(gregtechId("gas"),
            MaterialIconType.gas,
            m -> {
                FluidProperty property = m.getProperty(PropertyKey.FLUID);
                if (property != null && property.getPrimaryKey() != FluidStorageKeys.GAS) {
                    return "gas." + m.getName();
                }
                return m.getName();
            },
            m -> {
                if (m.hasProperty(PropertyKey.DUST)) {
                    return "gregtech.fluid.gas_vapor";
                }

                FluidProperty property = m.getProperty(PropertyKey.FLUID);
                if (m.isElement() || (property != null && property.getPrimaryKey() != FluidStorageKeys.LIQUID)) {
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
}
