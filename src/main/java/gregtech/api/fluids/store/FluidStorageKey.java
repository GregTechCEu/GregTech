package gregtech.api.fluids.store;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidStorageKey {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(gregtechId("liquid"),
            MaterialIconType.liquid,
            UnaryOperator.identity(),
            m -> m.hasProperty(PropertyKey.DUST) ? "gregtech.fluid.liquid_generic" : "gregtech.fluid.generic");
    public static final FluidStorageKey GAS = new FluidStorageKey(gregtechId("gas"),
            MaterialIconType.gas,
            UnaryOperator.identity(),
            m -> m.hasProperty(PropertyKey.DUST) ? "gregtech.fluid.gas_generic" : "gregtech.fluid.generic");
    public static final FluidStorageKey PLASMA = new FluidStorageKey(gregtechId("plasma"),
            MaterialIconType.plasma,
            s -> "plasma." + s, m -> "gregtech.fluid.plasma");

    private final ResourceLocation resourceLocation;
    private final MaterialIconType iconType;
    private final UnaryOperator<String> registryNameOperator;
    private final Function<Material, String> translationKeyFunction;
    private final int hashCode;

    public FluidStorageKey(@NotNull ResourceLocation resourceLocation, @NotNull MaterialIconType iconType,
                           @NotNull UnaryOperator<@NotNull String> registryNameOperator,
                           @NotNull Function<@NotNull Material, @NotNull String> translationKeyFunction) {
        this.resourceLocation = resourceLocation;
        this.iconType = iconType;
        this.registryNameOperator = registryNameOperator;
        this.translationKeyFunction = translationKeyFunction;
        this.hashCode = resourceLocation.hashCode();
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }

    public @NotNull MaterialIconType getIconType() {
        return this.iconType;
    }

    /**
     * @param baseName the base name of the fluid
     * @return the registry name to use
     */
    public @NotNull String getRegistryNameFor(@NotNull String baseName) {
        return registryNameOperator.apply(baseName);
    }

    /**
     * @return the translation key for fluids with this key
     */
    public @NotNull String getTranslationKeyFor(@NotNull Material material) {
        return this.translationKeyFunction.apply(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidStorageKey fluidKey = (FluidStorageKey) o;

        return resourceLocation.equals(fluidKey.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidStorageKey{" + resourceLocation + '}';
    }
}
